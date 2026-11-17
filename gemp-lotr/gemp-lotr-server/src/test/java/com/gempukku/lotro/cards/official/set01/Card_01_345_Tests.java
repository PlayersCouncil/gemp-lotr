package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_345_Tests
{

	// The Fellowship path with Mithril Mine as its site 4; a Nazgul, and a weapon of his in the discard pile.
	protected VirtualTableScenario GetMineScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("lemenya", "1_232");    // Ulaire Lemenya: Nazgul; her text is about [wraith] conditions on companions
					put("blade", "1_216");      // Morgul Blade: [wraith] hand weapon, cost 1, bearer must be a Nazgul
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_345");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The same path, with Shadow weapons of three kinds, cards that fail one word each, and bearers for all of them.
	protected VirtualTableScenario GetWeaponsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "1_190");        // Moria Axe: [moria] possession, hand weapon, cost 1, bearer must be a [moria] Orc
					put("bow", "7_155");        // Raider Bow: [raider] possession, ranged weapon, cost 2, bearer must be a [raider] Man
					put("sword", "2_50");       // The Balrog's Sword: [moria] ARTIFACT, hand weapon, cost 1, bearer must be The Balrog
					put("chain", "2_53");       // Cave Troll's Chain: [moria] possession that is no weapon, cost 1, on Cave Troll of Moria
					put("dwarvenaxe", "1_9");   // Dwarven Axe: a FREE PEOPLES hand weapon, cost 0, bearer must be a Dwarf
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] hand weapon, cost 0; laid in the OTHER player's discard pile
					put("runner", "1_178");     // Goblin Runner: [moria] Orc; its (2) only when played
					put("axeman", "4_224");     // Easterling Axeman: [raider] Man; his text wants 2 burdens
					put("balrog", "2_52");      // The Balrog, Flame of Udun: discarded if not underground; a Shadow ability used only on purpose
					put("troll", "1_165");      // Cave Troll of Moria, Scourge of the Black Pit: no ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion; a skirmish ability only
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					put("site4", "1_345");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MithrilMineStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Mithril Mine
		 * Unique: False
		 * Side: 
		 * Culture: 
		 * Shadow Number: 3
		 * Type: Site
		 * Subtype: 
		 * Block: Fellowship
		 * Site Number: 4
		 * Direction: Right
		 * Game Text: <b>Underground</b>. <b>Shadow:</b> Remove (1) to play a Shadow weapon from your discard pile.
		*/

		var scn = GetMineScenario();

		var card = scn.GetFreepsSite(4);

		assertEquals("Mithril Mine", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.UNDERGROUND));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(4, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.FELLOWSHIP, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.RIGHT, card.getBlueprint().getSiteDirection());
	}

	// "Remove (1) to play a Shadow weapon from your discard pile": a play from a pile as an effect is treated as
	// a cost, so the pool must pay for the removal and the weapon together (F-0046, F-0141).  The definition did
	// not count the (1) it removes: with exactly the weapon's cost in the pool the action was offered, took the
	// token and played nothing.
	// F-0450.
	@Test
	public void ThePoolMustPayForTheRemovalAndTheWeaponTogether() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMineScenario();

		var lemenya = scn.GetShadowCard("lemenya");
		var blade = scn.GetShadowCard("blade");
		scn.MoveCardsToDiscard(blade);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		var mine = scn.GetCurrentSite();
		scn.MoveMinionsToTable(lemenya);

		// (1) in the pool: enough for the removal or for the Blade, not for both.
		scn.SetTwilight(1);
		scn.ShadowRefreshDecision();
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mine));

		scn.SetTwilight(2);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(mine));
		scn.ShadowUseCardAction(mine);

		assertEquals(0, scn.GetTwilight());
		assertAttachedTo(blade, lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityRemoves1ToPlayAShadowWeaponOfAnyKindFromYourDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetWeaponsScenario();

		var axe = scn.GetShadowCard("axe");
		var bow = scn.GetShadowCard("bow");
		var sword = scn.GetShadowCard("sword");
		var chain = scn.GetShadowCard("chain");
		var dwarvenaxe = scn.GetShadowCard("dwarvenaxe");
		var freepsScimitar = scn.GetFreepsCard("scimitar");
		var runner = scn.GetShadowCard("runner");
		var axeman = scn.GetShadowCard("axeman");
		var balrog = scn.GetShadowCard("balrog");
		var troll = scn.GetShadowCard("troll");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToDiscard(chain, dwarvenaxe, freepsScimitar);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsPass();
		assertEquals(4, scn.GetCurrentSiteNumber());
		var mine = scn.GetCurrentSite();
		// Mithril Mine is underground: The Balrog stays in play there, and could bear his Sword.
		scn.MoveMinionsToTable(runner, axeman, balrog, troll);
		scn.SetTwilight(10);
		scn.ShadowRefreshDecision();
		assertInZone(Zone.SHADOW_CHARACTERS, balrog);

		// A Shadow possession that is no weapon and a Free Peoples weapon in his discard pile, and a Shadow weapon
		// in the other player's: nothing he may play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mine));

		scn.MoveCardsToDiscard(axe, bow, sword);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowActionAvailable(mine));
		scn.ShadowUseCardAction(mine);
		// A possession hand weapon, a ranged weapon of another culture and an artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, bow, sword));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(chain, dwarvenaxe, freepsScimitar));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(bow);
		// 10 - 1 removed - 2 for Raider Bow, which goes on the one [raider] Man.
		assertEquals(7, scn.GetTwilight());
		assertAttachedTo(bow, axeman);
		assertInDiscard(axe, sword, chain, dwarvenaxe, freepsScimitar);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// In the maneuver phase it is not offered, with the pool and the discard pile able to pay for it.
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(7, scn.GetTwilight());
		assertFalse(scn.ShadowActionAvailable(mine));
	}
}
