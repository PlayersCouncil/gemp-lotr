package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_139_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("defeat", "12_139");

					// Murderous Uruk: an [uruk-hai] Uruk-hai, strength 8, damage +1 and muster (a regroup matter) only.
					put("uruk", "11_195");
					// Saruman, Agent of the Dark Lord: an [uruk-hai] minion who is no Uruk-hai (a Wizard), strength 8, a
					// lurker, so his skirmish comes last.  His skirmish ability and his heal on a companion's death are
					// never reached.
					put("saruman", "12_144");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, strength 9, damage +1 and no other text.
					put("cuguard", "10_81");
					// Weapon of Opportunity: an [uruk-hai] hand weapon possession (strength +2) on an [uruk-hai] minion;
					// its draw is only on its own play.
					put("weapon", "12_159");

					// Five companions beside the Ring-bearer, none with text that reaches a test.
					// Aragorn, Ranger of the North: strength 8, vitality 4; his maneuver ability is never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: strength 6, vitality 3; his skirmish ability is never used.
					put("gimli", "1_13");
					// Dwarf Guard: its "To play, spot a Dwarf" is passed over by the cheat.
					put("guard", "1_7");
					// Elven Scout (two copies): muster while another Elf is spotted, a regroup matter.
					put("scout", "11_18");
					put("scout2", "11_18");
					// Farmer Maggot, Chaser of Rascals: a Free Peoples character who is no companion (an ally).
					put("maggot", "1_288");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BrokeninDefeatStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Broken in Defeat
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [uruk-hai] minion strength +2. Then you may spot 6 companions to return this event to your hand.
		*/

		var scn = GetScenario();

		var defeat = scn.GetFreepsCard("defeat");

		assertEquals("Broken in Defeat", defeat.getBlueprint().getTitle());
		assertNull(defeat.getBlueprint().getSubtitle());
		assertFalse(defeat.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, defeat.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, defeat.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, defeat.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(defeat, Timeword.SKIRMISH));
		assertEquals(1, defeat.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnUrukHaiMinionStrengthPlus2AndWithSixCompanionsReturnsToHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var scout2 = scn.GetFreepsCard("scout2");
		scn.MoveCompanionsToTable(aragorn, gimli, guard, scout, scout2);

		var defeat = scn.GetShadowCard("defeat");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");
		var cuguard = scn.GetShadowCard("cuguard");
		var weapon = scn.GetShadowCard("weapon");
		scn.MoveCardsToHand(defeat);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, saruman, cuguard);
		scn.AttachCardsTo(uruk, weapon);
		assertAttachedTo(weapon, uruk);
		assertInHand(defeat);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uruk},
				new PhysicalCardImpl[]{gimli, saruman});
		// The Guard goes onto Aragorn as well: a [sauron] Uruk-hai in the same skirmish.
		scn.ShadowAssignToMinions(aragorn, cuguard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(1, scn.GetShadowHandCount());
		assertEquals(10, scn.GetStrength(uruk));
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.ShadowPlayAvailable(defeat));
		scn.ShadowPlayCard(defeat);

		// Either [uruk-hai] minion, in this skirmish or not; not the [sauron] Uruk-hai, the [uruk-hai] weapon or a companion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(uruk, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(cuguard, weapon, aragorn));
		scn.ShadowChooseCard(uruk);
		assertEquals(12, scn.GetStrength(uruk));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(9, scn.GetStrength(cuguard));

		// Six companions, the Ring-bearer among them: the return is asked, and taken.
		assertTrue(scn.ShadowDecisionAvailable("return this event"));
		scn.ShadowChooseYes();
		assertInHand(defeat);
		assertEquals(1, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ChosenForALaterSkirmishTheBonusIsGoneThereAndDecliningTheReturnDiscardsTheEvent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var scout2 = scn.GetFreepsCard("scout2");
		scn.MoveCompanionsToTable(aragorn, gimli, guard, scout, scout2);

		var defeat = scn.GetShadowCard("defeat");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(defeat);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, saruman);
		assertInHand(defeat);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, uruk},
				new PhysicalCardImpl[]{gimli, saruman});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(saruman));
		scn.ShadowPlayCard(defeat);
		scn.ShadowChooseCard(saruman);
		assertEquals(10, scn.GetStrength(saruman));

		// Declined: the event goes to the discard pile and the hand is as it was.
		assertTrue(scn.ShadowDecisionAvailable("return this event"));
		scn.ShadowChooseNo();
		assertInDiscard(defeat);
		assertEquals(0, scn.GetShadowHandCount());

		// Still in force at the next decision of this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(saruman));

		// 8 against 8: Aragorn loses and lives.  In Saruman's own skirmish, a phase of its own, the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(saruman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsNotPlayableInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var defeat = scn.GetShadowCard("defeat");
		var uruk = scn.GetShadowCard("uruk");
		scn.MoveCardsToHand(defeat);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk);
		assertInHand(defeat);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(defeat));
	}

	@Test
	public void WithFiveCompanionsAndAnAllyTheReturnIsNotAskedAndTheEventIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var maggot = scn.GetFreepsCard("maggot");
		scn.MoveCompanionsToTable(aragorn, gimli, guard, scout);
		scn.MoveAlliesToTable(maggot);

		var defeat = scn.GetShadowCard("defeat");
		var uruk = scn.GetShadowCard("uruk");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(defeat);

		scn.StartGame();
		scn.MoveMinionsToTable(uruk, saruman);
		assertInHand(defeat);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, uruk);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(uruk));
		scn.ShadowPlayCard(defeat);
		scn.ShadowChooseCard(uruk);
		assertEquals(10, scn.GetStrength(uruk));

		// Five companions (the ally is no sixth): nothing is asked.
		assertFalse(scn.ShadowDecisionAvailable("return this event"));
		assertInDiscard(defeat);
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoUrukHaiMinionItIsPlayedForNothingAndTheReturnIsStillOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var guard = scn.GetFreepsCard("guard");
		var scout = scn.GetFreepsCard("scout");
		var scout2 = scn.GetFreepsCard("scout2");
		scn.MoveCompanionsToTable(aragorn, gimli, guard, scout, scout2);

		var defeat = scn.GetShadowCard("defeat");
		var cuguard = scn.GetShadowCard("cuguard");
		scn.MoveCardsToHand(defeat);

		scn.StartGame();
		scn.MoveMinionsToTable(cuguard);
		assertInHand(defeat);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, cuguard);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// No [uruk-hai] minion: the target is no requirement.
		assertTrue(scn.ShadowPlayAvailable(defeat));
		scn.ShadowPlayCard(defeat);
		assertEquals(9, scn.GetStrength(cuguard));

		// "Then" is carried out whatever the first sentence did.
		assertTrue(scn.ShadowDecisionAvailable("return this event"));
		scn.ShadowChooseYes();
		assertInHand(defeat);
		assertEquals(9, scn.GetStrength(cuguard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
