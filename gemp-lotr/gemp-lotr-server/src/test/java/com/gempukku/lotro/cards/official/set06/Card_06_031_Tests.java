package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_031_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("glamdring", "6_31");
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: vitality 4; a strength bonus only
					put("avenger", "6_27");       // Ent Avenger: a [gandalf] companion who is not Gandalf
					put("merry", "1_302");        // Merry, Friend to Sam: a second companion to skirmish; his ability is used only on purpose

					// Spells and an event that is none
					put("servant1", "1_83");      // Servant of the Secret Fire: Skirmish spell, spot Gandalf to make a minion strength -3
					put("servant2", "1_83");
					put("wizard", "1_78");        // Mysterious Wizard: Skirmish spell, Gandalf strength +2 (+4 at 4 or fewer burdens)
					put("wielder", "2_28");       // Wielder of the Flame: Maneuver spell, spot Gandalf to make a companion defender +1
					put("earth", "4_105");        // Under the Living Earth: a [gandalf] Skirmish event that is no spell

					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
					put("runner", "1_178");       // Goblin Runner: strength 5, vitality 1; adds (2) when played, never played here
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GlamdringStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Glamdring, Lightning Brand
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be Gandalf.
		 * He is damage +1.
		 * Each time you play a spell during a skirmish, you may make Gandalf damage +1 until the end of that skirmish.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("glamdring");

		assertEquals("Glamdring", card.getBlueprint().getTitle());
		assertEquals("Lightning Brand", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnlyOnGandalfAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(avenger);
		scn.MoveCardsToHand(gandalf, glamdring);

		scn.StartGame();

		// Frodo and a [gandalf] companion of another title: nobody may bear it.
		assertFalse(scn.FreepsPlayAvailable(glamdring));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(gandalf);
		assertTrue(scn.FreepsPlayAvailable(glamdring));
		assertEquals(0, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		// Gandalf is the only bearer it may have, so nobody is asked.
		scn.FreepsPlayCard(glamdring);
		assertAttachedTo(glamdring, gandalf);
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(avenger, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachSpellPlayedDuringASkirmishMayMakeGandalfDamagePlus1UntilTheEndOfThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var servant1 = scn.GetFreepsCard("servant1");
		var servant2 = scn.GetFreepsCard("servant2");
		var wizard = scn.GetFreepsCard("wizard");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(servant1, servant2, wizard);

		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		scn.MoveMinionsToTable(troop, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner}, new PhysicalCardImpl[]{gandalf, troop});
		scn.FreepsResolveSkirmish(merry);

		// A spell played in a skirmish that is not Gandalf's: "a skirmish" is any skirmish.
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		scn.FreepsPlayCard(servant1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsAcceptOptionalTrigger();
		scn.FreepsChooseCard(runner);
		assertEquals(2, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Merry 3 against the Runner 5 - 3 = 2: the Runner dies, and that skirmish is over.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gandalf);
		// The bonus ended with that skirmish.
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		// Two spells in Gandalf's own skirmish: offered each time, and the bonuses add up.
		scn.FreepsPlayCard(wizard);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		scn.ShadowPass();
		scn.FreepsPlayCard(servant2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(3, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NothingIsOfferedForASpellOutsideASkirmishOrAnEventThatIsNoSpellAndTheOfferMayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var glamdring = scn.GetFreepsCard("glamdring");
		var gandalf = scn.GetFreepsCard("gandalf");
		var wielder = scn.GetFreepsCard("wielder");
		var earth = scn.GetFreepsCard("earth");
		var servant1 = scn.GetFreepsCard("servant1");
		scn.MoveCompanionsToTable(gandalf);
		scn.AttachCardsTo(gandalf, glamdring);
		scn.MoveCardsToHand(wielder, earth, servant1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(glamdring, gandalf);
		scn.MoveMinionsToTable(troop);

		scn.SkipToPhase(Phase.MANEUVER);
		// An empty pool, so that the Shadow player cannot remove (3) to prevent the spell.
		scn.SetTwilight(0);
		scn.FreepsRefreshDecision();

		// A spell in the maneuver phase.
		scn.FreepsPlayCard(wielder);
		scn.FreepsChooseCard(gandalf);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gandalf, troop);
		scn.FreepsResolveSkirmish(gandalf);

		// A [gandalf] skirmish event that is no spell.
		// Its effect asks its count with no offer of Glamdring's ahead of it.
		scn.FreepsPlayCard(earth);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		assertTrue(scn.FreepsDecisionAvailable("Choose how many twilight tokens to spot"));
		scn.FreepsChoose("0");
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));

		// A spell in the skirmish, and the offer declined.
		scn.ShadowPass();
		scn.FreepsPlayCard(servant1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(glamdring));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetKeywordCount(gandalf, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
