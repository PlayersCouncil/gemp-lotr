package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

public class Card_07_320_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "7_320");
					// Companions, so that threats can be added: a threat cannot be added while there are as many as
					// there are companions.
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, strength 3; his abilities are used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: an Orc minion, strength 5, vitality 1; its text answers only its play
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Merry, Rohirrim Squire
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Gandalf
		 * Game Text: <b>Skirmish:</b> If you cannot spot 3 threats, add a threat to make Merry strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Rohirrim Squire", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.GANDALF, card.getBlueprint().getSignet());
	}

	@Test
	public void BelowThreeThreatsAddsAThreatToMakeMerryStrength2MoreForTheSkirmishAndAtThreeItIsNoLongerOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, sam, aragorn);

		scn.StartGame();
		scn.AddThreats(1);
		// Nearly counting, beside the threats: a burden on the Ring-bearer.
		scn.AddBurdens(1);
		assertEquals(1, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		scn.MoveMinionsToTable(runner, runner2);

		// Merry is left unassigned: the text names no skirmish of his.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner}, new PhysicalCardImpl[]{aragorn, runner2});
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		// The threat is the cost.  Merry alone is made stronger, and the burden stays as it was.
		assertEquals(2, scn.GetThreats());
		assertEquals(1, scn.GetBurdens());
		assertEquals(5, scn.GetStrength(merry));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(aragorn));
		// Still in force at the Shadow player's next decision of the same skirmish.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Sam 3 loses to the Runner's 5 and takes a wound; nobody dies.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(aragorn);

		// Each skirmish is a phase of its own: in Aragorn's, Merry is himself again.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
		// Two threats: still fewer than 3 to spot.
		assertEquals(2, scn.GetThreats());
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);
		assertEquals(3, scn.GetThreats());
		assertEquals(5, scn.GetStrength(merry));

		// Three threats can be spotted now.  With four companions a fourth threat could still be added: it is
		// the spot that withholds the ability.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
	}

	@Test
	public void NotOfferedWhenNoThreatCanBeAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(merry);

		scn.StartGame();
		// Two threats and two companions: fewer than 3 threats to spot, and no room for another.
		scn.AddThreats(2);
		assertEquals(2, scn.GetThreats());
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner);
		scn.FreepsResolveSkirmish(merry);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(merry));
		assertFalse(scn.FreepsActionAvailable(merry));
	}
}
