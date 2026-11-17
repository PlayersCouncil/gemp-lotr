package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_025_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("nocked", "11_25");
					put("nocked2", "11_25");
					// Legolas, Companion of the Ring: an Elf companion of resistance 7; his own text needs a battleground or a forest
					put("legolas", "11_21");
					// Elven Scout: an Elf companion of resistance 7; muster (a regroup matter) while another Elf is spotted
					put("scout", "11_18");
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: an Elf companion of resistance 6; +3 against a Nazgûl only
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an Elf ally (resistance 0); a start-of-turn heal and a fellowship ability only
					put("troop1", "1_177");     // Goblin Patrol Troop: a [moria] Orc, vitality 3, no text
					put("troop2", "1_177");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, vitality 3, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NockedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Nocked
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: Exert an Elf who has resistance 4 or more to wound a minion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("nocked");

		assertEquals("Nocked", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnElfOfResistance4OrMoreToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nocked = scn.GetFreepsCard("nocked");
		var legolas = scn.GetFreepsCard("legolas");
		var scout = scn.GetFreepsCard("scout");
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, scout, arwen);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(nocked);

		scn.StartGame();
		// Each burden takes 1 from every companion's resistance: Legolas and the Scout 7 - 3, Arwen 6 - 3, Frodo 10 - 3.
		scn.AddBurdens(3);
		var troop1 = scn.GetShadowCard("troop1");
		var troop2 = scn.GetShadowCard("troop2");
		var savage = scn.GetShadowCard("savage");
		scn.MoveMinionsToTable(troop1, troop2, savage);
		// Vitality 3 with 2 wounds: one more wound kills this Troop.
		scn.AddWoundsToChar(troop2, 2);
		assertEquals(2, scn.GetWoundsOn(troop2));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(4, scn.GetResistance(legolas));
		assertEquals(4, scn.GetResistance(scout));
		assertEquals(3, scn.GetResistance(arwen));
		assertEquals(7, scn.GetResistance(frodo));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsPlayAvailable(nocked));

		scn.FreepsPlayCard(nocked);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, scout));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(arwen, elrond, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(scout));

		// The exhausted Troop is offered too: a wound needs no vitality to spare.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop1, troop2, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(legolas, scout, arwen, elrond, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop2);

		assertInDiscard(troop2);
		assertEquals(0, scn.GetWoundsOn(troop1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(nocked);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenNoElfHasResistance4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nocked = scn.GetFreepsCard("nocked");
		var legolas = scn.GetFreepsCard("legolas");
		var scout = scn.GetFreepsCard("scout");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(legolas, scout);
		scn.MoveCardsToHand(nocked);

		scn.StartGame();
		// Four burdens: the two Elves at 7 - 4, one short; Frodo, no Elf, at 10 - 4.
		scn.AddBurdens(4);
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveMinionsToTable(troop1);
		assertInHand(nocked);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(3, scn.GetResistance(legolas));
		assertEquals(3, scn.GetResistance(scout));
		assertEquals(6, scn.GetResistance(frodo));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(nocked));
		assertEquals(0, scn.GetWoundsOn(troop1));
	}

	@Test
	public void CannotBePlayedWhenTheOnlyElfOfResistance4IsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nocked = scn.GetFreepsCard("nocked");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(legolas, arwen);
		scn.MoveCardsToHand(nocked);
		// Vitality 3 with 2 wounds: Legolas cannot exert.  Arwen could, but has resistance 3 below.
		scn.AddWoundsToChar(legolas, 2);

		scn.StartGame();
		scn.AddBurdens(3);
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveMinionsToTable(troop1);
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
		assertInHand(nocked);

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(4, scn.GetResistance(legolas));
		assertEquals(3, scn.GetResistance(arwen));
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(nocked));
		assertEquals(2, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
	}

	@Test
	public void StillPlayedAndTheElfStillExertedWhenNoMinionIsLeftToWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var nocked = scn.GetFreepsCard("nocked");
		var nocked2 = scn.GetFreepsCard("nocked2");
		var legolas = scn.GetFreepsCard("legolas");
		var scout = scn.GetFreepsCard("scout");
		scn.MoveCompanionsToTable(legolas, scout);
		scn.MoveCardsToHand(nocked, nocked2);

		scn.StartGame();
		var troop1 = scn.GetShadowCard("troop1");
		scn.MoveMinionsToTable(troop1);
		// Vitality 3 with 2 wounds: the first Nocked kills the only minion.
		scn.AddWoundsToChar(troop1, 2);
		assertEquals(2, scn.GetWoundsOn(troop1));

		scn.SkipToPhase(Phase.ARCHERY);
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(scout));
		scn.FreepsPlayCard(nocked);
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, scout));
		scn.FreepsChooseCard(legolas);
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInDiscard(troop1);
		assertInDiscard(nocked);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());

		// No minion is left in play: the second Nocked is offered all the same, its Elf exerted for nothing.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsPlayAvailable(nocked2));
		scn.FreepsPlayCard(nocked2);
		scn.FreepsChooseCard(scout);
		assertEquals(1, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetWoundsOn(legolas));
		assertInDiscard(nocked2);
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
