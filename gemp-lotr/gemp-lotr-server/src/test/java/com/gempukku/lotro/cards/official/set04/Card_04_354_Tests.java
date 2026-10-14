package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_354_Tests
{

	// The Towers path with Hornburg Armory as its site 6; the tests use the copy on the path.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Horn of Helm: unique [dwarven] possession for the support area, cost 2; a maneuver ability only
					put("horn", "5_8");
					put("horn2", "5_8");
					put("sword", "1_299");      // Hobbit Sword: [shire] possession on a Hobbit, no other text
					// Mithril-coat: [shire] artifact on Frodo; archery and skirmish texts only
					put("mithril", "2_105");
					put("lord", "1_21");        // Lord of Moria: a Free Peoples condition for the support area; each Dwarf damage +1
					put("legolas", "1_50");     // Legolas, Greenleaf: a companion; an archery ability
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: the cards laid on top of the draw deck
					put("aragorn2", "1_89");
					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text
				}},
				new HashMap<>() {{
					putAll(VirtualTableScenario.TowersSites);
					put("site6", "4_354");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornburgArmoryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Hornburg Armory
		 * Unique: False
		 * Shadow Number: 3
		 * Type: Site
		 * Block: Towers
		 * Direction: Left
		 * Site Number: 6T
		 * Game Text: <b>Sanctuary</b>. <b>Fellowship:</b> Play a possession to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(6);

		assertEquals("Hornburg Armory", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SANCTUARY));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getSiteNumber());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
	}

	@Test
	public void FellowshipActionPlaysAPossessionFromHandToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var horn = scn.GetFreepsCard("horn");
		var sword = scn.GetFreepsCard("sword");
		var mithril = scn.GetFreepsCard("mithril");
		var lord = scn.GetFreepsCard("lord");
		var legolas = scn.GetFreepsCard("legolas");
		var aragorn = scn.GetFreepsCard("aragorn");
		var aragorn2 = scn.GetFreepsCard("aragorn2");

		scn.StartGame();
		scn.SkipToSite(6);
		var armory = scn.GetCurrentSite();
		assertEquals("Hornburg Armory", armory.getBlueprint().getTitle());
		scn.SetFreepsHand(horn, sword, mithril, lord, legolas);
		scn.MoveCardsToTopOfDeck(aragorn2, aragorn);
		scn.FreepsRefreshDecision();
		int deck = scn.GetFreepsDeckCount();
		assertEquals(5, scn.GetFreepsHandCount());

		// Each card in hand could be played from it.
		assertTrue(scn.FreepsPlayAvailable(mithril));
		assertTrue(scn.FreepsPlayAvailable(lord));
		assertTrue(scn.FreepsPlayAvailable(legolas));
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);

		// Possessions of two cultures, one for a bearer and one for the support area; not an artifact, a condition
		// or a companion, each of which could be played.
		assertTrue(scn.FreepsHasCardChoiceAvailable(horn, sword));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(mithril, lord, legolas));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sword);

		// Frodo is the one Hobbit, so the sword goes on him with nothing asked; the top card is drawn.
		assertAttachedTo(sword, frodo);
		assertInHand(aragorn);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Again, with the Horn the one possession left.
		assertTrue(scn.FreepsActionAvailable(armory));
		scn.FreepsUseCardAction(armory);
		assertInZone(Zone.SUPPORT, horn);
		assertInHand(aragorn2);
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoPossessionInHandThatCanBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var horn2 = scn.GetFreepsCard("horn2");
		var legolas = scn.GetFreepsCard("legolas");

		scn.StartGame();
		scn.SkipToSite(6);
		var armory = scn.GetCurrentSite();
		// A possession that cannot be played (Horn of Helm is unique and in play), and a companion who can.
		scn.MoveCardsToSupportArea(horn);
		scn.SetFreepsHand(horn2, legolas);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(legolas));
		assertFalse(scn.FreepsPlayAvailable(horn2));
		assertFalse(scn.FreepsActionAvailable(armory));
	}

	@Test
	public void IsNotOfferedInTheManeuverPhaseAtHornburgArmory() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		scn.StartGame();
		scn.SkipToSite(5);
		scn.SetFreepsHand(sword);
		scn.MoveMinionsToTable(scn.GetShadowCard("troop"));
		assertInHand(sword);

		// The fellowship moves on to site 6, where the maneuver phase is played.
		scn.SkipToPhase(Phase.MANEUVER);
		var armory = scn.GetCurrentSite();
		assertEquals("Hornburg Armory", armory.getBlueprint().getTitle());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsActionAvailable(armory));
		assertInHand(sword);
	}
}
