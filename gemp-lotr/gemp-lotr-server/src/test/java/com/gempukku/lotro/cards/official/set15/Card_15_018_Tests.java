package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_018_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("legolas", "15_18");

					put("rumil", "17_12");      // Rumil, Silvan Elf: [elven] Elf companion, Hunter 1; his texts need a wounded minion or the archery phase
					put("faramir", "18_48");    // Faramir, Captain of Ithilien: [gondor] Man companion, Ranger, Hunter 2; a maneuver heal never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion who is no hunter; a maneuver ability only
					put("uruk", "15_171");      // Tracking Uruk: [uruk-hai] minion, Hunter 1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LegolasStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Legolas, Fleet-footed Hunter
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Elf
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Hunter 1</b>. <helper>(While skirmishing a non-hunter character, this character is strength +1.)</helper><br><b>Regroup:</b> Exert 2 hunter companions to draw a card.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("legolas");

		assertEquals("Legolas", card.getBlueprint().getTitle());
		assertEquals("Fleet-footed Hunter", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.ELF, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.HUNTER));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.HUNTER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void RegroupAbilityExertsAnyTwoHunterCompanionsToDrawACard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var faramir = scn.GetFreepsCard("faramir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var uruk = scn.GetShadowCard("uruk");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, rumil, faramir, aragorn);

		scn.StartGame();
		// A hunter who is no companion, left unassigned so that he is still in play in the regroup phase.
		scn.MoveMinionsToTable(uruk);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertInPlay(uruk);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		int hand = scn.GetFreepsHandCount();
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(legolas));

		scn.FreepsUseCardAction(legolas);
		// Three hunter companions of three titles, two cultures and two races; not Aragorn or Frodo, who are no
		// hunters, nor the Tracking Uruk, a hunter who is no companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, rumil, faramir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, frodo, uruk));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());

		// Legolas need not be one of the two.
		scn.FreepsChooseCards(rumil, faramir);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(rumil));
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(hand + 1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileOnlyOneHunterCompanionCanExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var legolas = scn.GetFreepsCard("legolas");
		var rumil = scn.GetFreepsCard("rumil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(legolas, rumil, aragorn);

		scn.StartGame();
		// Rumil, the only other hunter companion, is exhausted (vitality 3, 2 wounds). Legolas could exert twice and
		// Aragorn, who is no hunter, could exert: neither pays for two hunter companions.
		scn.AddWoundsToChar(rumil, 2);
		assertEquals(2, scn.GetWoundsOn(rumil));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(legolas));
	}
}
