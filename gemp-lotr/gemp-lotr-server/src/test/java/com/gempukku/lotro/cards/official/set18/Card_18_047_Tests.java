package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_047_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("army", "18_47");

					// Aragorn, Ranger of the North: a [gondor] Man companion, vitality 4; a maneuver ability used
					// only on purpose
					put("aragorn", "1_89");
					// Pippin, Trollslayer: a [gondor] companion who is a Hobbit; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Gondorian Merchant: a [gondor] ally, vitality 1; a maneuver ability that needs initiative
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so that the Merchant can exert;
					// its response answers a wound to its bearer
					put("narsil", "9_34");

					put("troop", "1_177");      // Goblin Patrol Troop: a minion with no text, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ElendilsArmyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Elendil's Army
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Follower
		 * Strength: +2
		 * Aid: Exert a [gondor] companion.
		 * Game Text: <b>Aid</b> – Exert a [gondor] companion. <helper>(At the start of the maneuver phase, you may exert a [gondor] companion to transfer this to a companion.)</helper>
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("army");

		assertEquals("Elendil's Army", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.FOLLOWER, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void AidExertsAGondorCompanionOfThePlayersChoiceToTransferElendilsArmyToACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var army = scn.GetFreepsCard("army");
		var aragorn = scn.GetFreepsCard("aragorn");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		scn.MoveCardsToSupportArea(army);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.SUPPORT, army);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merchant));
		// Narsil makes the Merchant vitality 2: he could exert, and only his not being a companion keeps him out.
		assertEquals(2, scn.GetVitality(merchant));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(army, "Aid"));

		scn.FreepsAcceptOptionalTrigger();
		// A [gondor] Man and a [gondor] Hobbit may pay; Frodo, a companion of another culture, and the
		// Merchant, a [gondor] ally, may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, merchant));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(merchant));

		// The Aid paid, the Army goes to the companion chosen.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo));
		scn.FreepsChooseCard(frodo);
		assertAttachedTo(army, frodo);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AidIsNotOfferedWhileTheOnlyGondorCompanionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var army = scn.GetFreepsCard("army");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(army);
		// Vitality 4 with 3 wounds: Aragorn cannot exert. Frodo can, but is no [gondor] companion.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");

		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(army, "Aid"));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.SUPPORT, army);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
	}
}
