package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_239_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sam", "1_311");         // Sam, Son of Hamfast: a Hobbit companion; abilities used only on purpose
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("legolas", "1_50");      // Legolas, Greenleaf: an Elf; an archery ability used only on purpose
					// Gondorian Merchant: a [gondor] Man ally (home 3T, in the support area); a maneuver ability used
					// only on purpose
					put("merchant", "7_97");
					put("sword", "1_299");       // Hobbit Sword: [shire] possession, hand weapon, on a Hobbit; no text
					put("bow", "1_41");          // Elven Bow: [elven] possession, ranged weapon, on an Elf; bearer is an archer
					// Narsil, Blade of the Faithful: [gondor] ARTIFACT hand weapon, on a [gondor] Man; its bonus is for
					// Elendil or Isildur only, and its response is never answered here
					put("narsil", "9_34");
					put("armor", "1_92");        // Armor: [gondor] possession, armor (no weapon), on a Man

					put("runner", "1_178");      // Goblin Runner: [moria] Orc; its only text is on being played
					// Goblin Scimitar: a Shadow weapon ([moria] possession, hand weapon, on a [moria] Orc); its only
					// text is on being played
					put("scimitar", "1_180");
				}},
				new HashMap<>()
				{{
					// Fangorn Glade is a Shadows-block site; each test lays the Shadow player's copy as the next site of
					// the Fellowship-block path (in place of site 9 in the adventure decks, which no test reaches).
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("glade", "11_239");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FangornGladeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Fangorn Glade
		 * Unique: False
		 * Type: Site
		 * Block: Shadows
		 * Shadow Number: 0
		 * Direction: Right
		 * Game Text: <b>Forest</b>. When the fellowship moves to this site, add (1) for each Free Peoples weapon.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsSite("glade");

		assertEquals("Fangorn Glade", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.SHADOWS, card.getBlueprint().getSiteBlock());
		// A Shadows-block site has no site number of its own.
		assertEquals(0, card.getBlueprint().getSiteNumber());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.FOREST));
	}

	@Test
	public void MovingThereWithOneWeaponAddsOneAndMovingOnAddsNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, sword);
		// Armor is a Free Peoples item, but no weapon.
		scn.AttachCardsTo(aragorn, armor);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("glade"));

		assertAttachedTo(sword, sam);
		assertAttachedTo(armor, aragorn);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		assertEquals("Fangorn Glade", scn.GetCurrentSite().getBlueprint().getTitle());
		// 0 for the site + 3 companions + 1 for the Hobbit Sword
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The fellowship moves on, from the Glade to Rivendell Valley (Shadow number 0): nothing for the weapon.
		scn.SkipToPhase(Phase.REGROUP);
		int before = scn.GetTwilight();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals(3, scn.GetCurrentSiteNumber());
		// 0 for Rivendell Valley + 3 companions
		assertEquals(before + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachFreePeoplesWeaponIsCountedPossessionOrArtifactAndWhoeverBearsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var legolas = scn.GetFreepsCard("legolas");
		var merchant = scn.GetFreepsCard("merchant");
		var sword = scn.GetFreepsCard("sword");
		var bow = scn.GetFreepsCard("bow");
		var narsil = scn.GetFreepsCard("narsil");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(sam, aragorn, legolas);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(sam, sword);      // a hand weapon possession on a companion
		scn.AttachCardsTo(legolas, bow);    // a ranged weapon possession on a companion
		scn.AttachCardsTo(merchant, narsil);  // an artifact weapon on an ally, in the support area
		scn.AttachCardsTo(aragorn, armor);  // no weapon

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("glade"));

		assertAttachedTo(sword, sam);
		assertAttachedTo(bow, legolas);
		assertAttachedTo(narsil, merchant);
		assertAttachedTo(armor, aragorn);
		assertEquals(0, scn.GetTwilight());
		scn.FreepsPass();

		assertEquals("Fangorn Glade", scn.GetCurrentSite().getBlueprint().getTitle());
		// 0 for the site + 4 companions + 3 for the Sword, the Bow and Narsil
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AShadowWeaponIsNotCountedWhenTheFellowshipMovesThereInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var sword = scn.GetFreepsCard("sword");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		scn.MoveCompanionsToTable(sam);
		scn.AttachCardsTo(sam, sword);

		scn.StartGame();
		assertAttachedTo(sword, sam);
		// The first move, to Ettenmoors (site 2), and no minion played: the turn goes on to its regroup phase.
		scn.FreepsPass();
		scn.SkipToPhase(Phase.REGROUP);
		// A minion can be in play at a move only in the regroup phase.
		scn.MoveMinionsToTable(runner);
		scn.AttachCardsTo(runner, scimitar);
		scn.MoveCardToAdventurePath(scn.GetShadowSite("glade"));

		assertAttachedTo(sword, sam);
		assertAttachedTo(scimitar, runner);
		int before = scn.GetTwilight();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		assertEquals("Fangorn Glade", scn.GetCurrentSite().getBlueprint().getTitle());
		// 0 for the site + 2 companions + 1 for the Hobbit Sword; nothing for the Goblin Scimitar
		assertEquals(before + 3, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
