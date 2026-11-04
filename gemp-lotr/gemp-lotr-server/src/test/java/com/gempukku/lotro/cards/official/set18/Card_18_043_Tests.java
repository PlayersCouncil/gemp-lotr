package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "18_43");

					put("denethor", "7_86");    // Denethor, Wizened Steward: [gondor] Man, strength 5, +1 for each other [gondor] Man you can spot (limit +4); that is his whole text
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion; Maneuver: exert Aragorn to make him defender +1
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion; Knight is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally; a maneuver ability used only on purpose
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only on purpose
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: a [rohan] Man; his strength text reads a skirmish

					put("runner", "1_178");     // Goblin Runner: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenethorsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Denethor's Sword
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be a [gondor] Man.<br>While bearer is Denethor, his game text does not apply.
		*/

		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertEquals("Denethor's Sword", sword.getBlueprint().getTitle());
		assertNull(sword.getBlueprint().getSubtitle());
		assertTrue(sword.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, sword.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAGondorManAndOnDenethorItTurnsOffHisGameText() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var frodo = scn.GetRingBearer();
		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(denethor, aragorn, knight, pippin, eomer);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(sword);
		assertInHand(sword);

		scn.StartGame();

		// 5, +3 for Aragorn, the Knight and the Merchant (Pippin is no Man, Eomer no [gondor] Man).
		assertEquals(8, scn.GetStrength(denethor));
		assertTrue(scn.FreepsPlayAvailable(sword));

		scn.FreepsPlayCard(sword);
		// [gondor] Men, companions and the ally; not the [gondor] Hobbit, the [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(denethor, aragorn, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, eomer, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(denethor);

		assertAttachedTo(sword, denethor);
		// His own text no longer applies: 5 and the Sword's +2, not the 10 it would be with his +3 still in force.
		assertEquals(7, scn.GetStrength(denethor));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BorneByAnotherGondorManItLeavesDenethorsTextAndTheBearersOwnApplying() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");
		var denethor = scn.GetFreepsCard("denethor");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		scn.MoveCompanionsToTable(denethor, aragorn, knight);
		scn.AttachCardsTo(aragorn, sword);
		assertAttachedTo(sword, aragorn);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// Denethor in play but not the bearer: his +1 for each of Aragorn and the Knight still applies.
		assertEquals(7, scn.GetStrength(denethor));

		scn.SkipToPhase(Phase.MANEUVER);
		// The bearer is no Denethor, and his own ability is still offered.
		assertEquals(7, scn.GetStrength(denethor));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertTrue(scn.FreepsActionAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));

		scn.FreepsUseCardAction(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
