package com.gempukku.lotro.cards.official.set19;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_19_002_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("gimli", "19_2");
					put("farin", "1_11");       // Farin, Dwarven Emissary: [dwarven] Dwarf companion; +2 against an Orc only
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; a maneuver ability never used
					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, armor, cost 0, on a Dwarf
					put("armor2", "1_8");
					put("armor3", "1_8");       // left in the draw deck, to be drawn
					put("handaxe", "2_10");     // Hand Axe: [dwarven] possession, hand weapon, cost 0, on a Dwarf; an archery ability never used
					put("handaxe2", "2_10");
					put("helm", "1_15");        // Gimli's Helm: [dwarven] possession, helm, cost 0, on Gimli; a skirmish ability never used
					put("horn", "5_8");         // Horn of Helm: [dwarven] possession, cost 2, to the support area; a maneuver ability never used
					put("ring", "9_9");         // Ring of Retribution: [dwarven] ARTIFACT, cost 0, on a Dwarf; a response to his win never met
					put("sword", "1_112");      // Ranger's Sword: [gondor] possession, cost 1, on Aragorn
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GimliStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Gimli, Opinionated Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Dwarf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: Fellowship. Damage +1.
		 * 	Each time you play a [dwarven] possession, you may draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("gimli");

		assertEquals("Gimli", card.getBlueprint().getTitle());
		assertEquals("Opinionated Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.DWARF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FELLOWSHIP));
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void EachTimeADwarvenPossessionIsPlayedACardMayBeDrawnUntilTheRuleOf4IsSpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var armor = scn.GetFreepsCard("armor");
		var armor2 = scn.GetFreepsCard("armor2");
		var handaxe = scn.GetFreepsCard("handaxe");
		var handaxe2 = scn.GetFreepsCard("handaxe2");
		var helm = scn.GetFreepsCard("helm");
		var horn = scn.GetFreepsCard("horn");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.MoveCardsToHand(armor, armor2, handaxe, handaxe2, helm, horn);

		scn.StartGame();

		assertEquals(6, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		// An armor borne by a Dwarf: offered, and taken.
		scn.FreepsPlayCard(armor);
		scn.FreepsChooseCard(gimli);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());

		// A possession of no class in the support area: offered again, and declined.
		scn.FreepsPlayCard(horn);
		assertInZone(Zone.SUPPORT, horn);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A hand weapon, and the same on Farin twice more: each offered and taken.
		scn.FreepsPlayCard(handaxe);
		scn.FreepsChooseCard(gimli);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsPlayCard(armor2);
		assertAttachedTo(armor2, farin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsPlayCard(handaxe2);
		scn.FreepsChooseCard(farin);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gimli));
		scn.FreepsAcceptOptionalTrigger();
		assertAttachedTo(handaxe, gimli);
		assertAttachedTo(handaxe2, farin);
		// 5 in hand, three played, three drawn: four cards drawn this fellowship phase.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());

		// The Rule of 4 is spent: the next [dwarven] possession played offers no draw.
		scn.FreepsPlayCard(helm);
		assertAttachedTo(helm, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarvenArtifactOrAPossessionOfAnotherCultureOffersNoDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var ring = scn.GetFreepsCard("ring");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(ring, sword);

		scn.StartGame();

		assertEquals(2, scn.GetFreepsHandCount());
		int deck = scn.GetFreepsDeckCount();

		// A [dwarven] card that is an artifact, not a possession.
		scn.FreepsPlayCard(ring);
		assertAttachedTo(ring, gimli);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A possession of another culture.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, aragorn);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ADwarvenPossessionTransferredIsNotPlayedAndOffersNoDraw() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var farin = scn.GetFreepsCard("farin");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, farin);
		scn.AttachCardsTo(gimli, armor);
		assertAttachedTo(armor, gimli);

		scn.StartGame();

		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();

		// A transfer in the fellowship phase is not a play (CR 4.2 "transfer").
		assertTrue(scn.FreepsTransferAvailable(armor));
		scn.FreepsTransferCard(armor);
		assertAttachedTo(armor, farin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(hand, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
