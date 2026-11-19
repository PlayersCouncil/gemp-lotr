package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_233_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fearless", "4_233");

					// Southron Explorer: [raider] Southron Man, strength 5, vitality 3; a regroup ability used only on purpose
					put("explorer", "4_250");
					// Bladetusk Rearguard: [raider] artifact whose maneuver ability (remove (5)) makes it a fierce
					// mounted Southron minion, strength 5, vitality 5, until the end of the turn: a Southron who is no Man
					put("rearguard", "103_43");
					// Easterling Infantry: [raider] Man who is no Southron, strength 8; fierce and damage +1 at 3 burdens
					put("infantry", "4_227");
					// Ulaire Attea, Khamul of the East: a [raider] minion who is no Man (a Nazgul); text only at 3 burdens
					put("khamul", "103_61");
					// Dunlending Brigand: a [dunland] Man minion, strength 8, with no text
					put("brigand", "4_10");

					// Aragorn, Ranger of the North: a [gondor] Man companion, strength 8; a maneuver ability used only on purpose
					put("aragorn", "1_89");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FearlessStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Fearless
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a Southron strength +1 for each burden you spot (limit +5), or make a [raider] Man strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fearless");

		assertEquals("Fearless", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndInASkirmishAnySouthronMayBeChosenForTheBurdenBonus() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var explorer = scn.GetShadowCard("explorer");
		var rearguard = scn.GetShadowCard("rearguard");
		var infantry = scn.GetShadowCard("infantry");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fearless);
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, infantry);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		// 5 for the Rearguard's ability and 2 to play Fearless.
		scn.SetTwilight(7);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		// A Southron minion now, and no Man.
		assertTrue(scn.HasKeyword(rearguard, Keyword.SOUTHRON));
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertFalse(scn.IsRace(rearguard, Race.MAN));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		// The phase next door to the skirmish.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(fearless));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{frodo, explorer}, new PhysicalCardImpl[]{aragorn, infantry});
		// The Rearguard is left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(fearless));
		scn.ShadowPlayCard(fearless);
		// A Southron and a [raider] Man are both there: the Shadow player is asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("Southron");

		// A Southron Man in this skirmish and a Southron who is no Man in none; not the [raider] Man who is no
		// Southron, nor a Free Peoples character.
		assertTrue(scn.ShadowHasCardChoiceAvailable(explorer, rearguard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(infantry, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		// The Shadow player is still choosing; the Free Peoples player is asked nothing.
		assertTrue(scn.ShadowAnyDecisionsAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
	}

	@Test
	public void MakesTheChosenSouthronStrongerByTheNumberOfBurdensNamedForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fearless);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, infantry);
		scn.AddBurdens(3);
		assertEquals(3, scn.GetBurdens());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, explorer}, new PhysicalCardImpl[]{frodo, infantry});
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(explorer));
		scn.ShadowPlayCard(fearless);
		scn.ShadowChoose("Southron");
		// The Explorer is the one Southron and is chosen unasked; nothing is added before the number is named.
		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(3, scn.ShadowGetChoiceMax());
		assertEquals(3, (int) scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("2");

		// 5 + 2 burdens named, of 3
		assertEquals(7, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Each skirmish is a phase of its own: in the next one (Aragorn, 8, has beaten him and he lives, wounded)
		// the bonus is gone.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(explorer));
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithSixBurdensNamedTheSouthronIsStrengthPlusFiveAndNoMore() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var explorer = scn.GetShadowCard("explorer");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(fearless);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer);
		scn.AddBurdens(6);
		assertEquals(6, scn.GetBurdens());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(frodo, explorer);
		scn.FreepsResolveSkirmish(frodo);
		scn.FreepsPass();

		scn.ShadowPlayCard(fearless);
		scn.ShadowChoose("Southron");
		assertEquals(5, scn.GetStrength(explorer));
		// The number is not capped; the bonus is.
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(6, scn.ShadowGetChoiceMax());
		assertEquals(6, (int) scn.ShadowGetChoiceDefault());
		scn.ShadowChoose("6");

		// 5 + 5 (limit +5), not + 6
		assertEquals(10, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithOnlyASouthronWhoIsNoManTheBurdenBonusIsTakenWithNoChoiceOfWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var rearguard = scn.GetShadowCard("rearguard");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fearless);
		scn.MoveCardsToSupportArea(rearguard);

		scn.StartGame();
		// The Brigand, no [raider] card, is there so that the game has a maneuver phase.
		scn.MoveMinionsToTable(brigand);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(7);
		scn.FreepsPass();
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rearguard);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(rearguard));
		scn.ShadowPlayCard(fearless);
		// No [raider] Man is in play, so the other half cannot be done: no choice of which.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(5, scn.GetStrength(rearguard));
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChoose("2");

		// 5 + 2
		assertEquals(7, scn.GetStrength(rearguard));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void TheRaiderManHalfMakesAnyRaiderManStrengthPlusTwoForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var explorer = scn.GetShadowCard("explorer");
		var infantry = scn.GetShadowCard("infantry");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fearless);

		scn.StartGame();
		scn.MoveMinionsToTable(explorer, infantry, khamul, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, infantry}, new PhysicalCardImpl[]{frodo, explorer});
		// Khamul and the Brigand are left out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(5, scn.GetStrength(explorer));
		assertEquals(8, scn.GetStrength(infantry));
		scn.ShadowPlayCard(fearless);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChoose("+2");

		// An Easterling in this skirmish and a Southron whose skirmish is still to come; not a [raider] minion who
		// is no Man, a Man of another culture, or a Free Peoples Man.
		assertTrue(scn.ShadowHasCardChoiceAvailable(infantry, explorer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(khamul, brigand, aragorn, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(explorer);

		// 5 + 2
		assertEquals(7, scn.GetStrength(explorer));
		assertEquals(8, scn.GetStrength(infantry));
		assertEquals(12, scn.GetStrength(khamul));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still in force at the next decision of this skirmish, and gone in the next.
		scn.FreepsPass();
		assertEquals(7, scn.GetStrength(explorer));
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(frodo);
		assertEquals(5, scn.GetStrength(explorer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoSouthronInPlayTheRaiderManHalfIsTakenWithNoChoiceOfWhich() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var infantry = scn.GetShadowCard("infantry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fearless);

		scn.StartGame();
		scn.MoveMinionsToTable(infantry);
		scn.AddBurdens(2);
		assertEquals(2, scn.GetBurdens());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, infantry);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(infantry));
		scn.ShadowPlayCard(fearless);

		// The other half has no Southron to act on: no choice of which, and the one [raider] Man is chosen unasked.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		// 8 + 2, not + 2 burdens
		assertEquals(10, scn.GetStrength(infantry));
		assertInDiscard(fearless);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNeitherASouthronNorARaiderManItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fearless = scn.GetShadowCard("fearless");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(fearless);

		scn.StartGame();
		scn.MoveMinionsToTable(khamul, brigand);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, brigand);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(12, scn.GetStrength(khamul));
		assertTrue(scn.ShadowPlayAvailable(fearless));
		scn.ShadowPlayCard(fearless);

		// Nothing on either half to act on: nothing is asked, and no strength changes.
		assertFalse(scn.ShadowDecisionAvailable("Choose action to perform"));
		assertEquals(8, scn.GetStrength(brigand));
		assertEquals(12, scn.GetStrength(khamul));
		assertInDiscard(fearless);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
