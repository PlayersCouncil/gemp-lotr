package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_048_Tests
{

	// The Fellowship path; the skirmishes are fought at site 2, Ettenmoors 1_331, whose skirmish ability is used
	// only on purpose.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("release", "15_48");
					put("release2", "15_48");

					put("smeagol", "6_45");         // Sméagol, Poor Creature: the [gollum] Sméagol, Ring-bound, resistance 6; a skirmish ability used only on purpose
					put("shiresmeagol", "103_116"); // Smeagol, Once Like Us: the [shire] Sméagol, Ring-bound, resistance 6; an assignment ability used only on purpose
					put("sam", "1_311");            // Sam, Son of Hamfast: a Ring-bound Hobbit who is not the Ring-bearer, strength 3, vitality 4, resistance 5
					put("merry", "1_302");          // Merry, Friend to Sam: an unbound Hobbit companion; a skirmish ability used only on purpose
					put("bounder", "1_286");        // Bounder: a Hobbit ally (no ally is Ring-bound); a skirmish ability used only on purpose
					put("boromir", "0_65");         // Boromir, Steward's Heir: a Ring-bound Man; keywords only
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability used only on purpose

					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: strength 5; a regroup ability used only on purpose
					// Dunlending Brigand: strength 8, vitality 1, no text.  Against a Hobbit of 3, more than double and
					// less than triple.
					put("brigand1", "4_10");
					put("brigand2", "4_10");
					put("party", "1_158");          // Uruk-hai Raiding Party: strength 9, damage +1 only; exactly triple a Hobbit of 3
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1; cheated in, so its text never comes up
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// Gimli, Bearer of Grudges 9_4 as Ring-bearer: no Ring-bound Hobbit is in play.  His start-of-skirmish text
	// answers only a skirmish he is in, and he fights none here.
	protected VirtualTableScenario GetNoHobbitScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("release", "15_48");
					put("smeagol", "6_45");     // Sméagol, Poor Creature: a Ring-bound companion who is no Hobbit
					put("merry", "1_302");      // Merry, Friend to Sam: an unbound Hobbit companion
					put("brigand1", "4_10");    // Dunlending Brigand: strength 8, vitality 1, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GimliRB,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ReleaseThemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Release Them
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gollum
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot Sméagol to prevent a Ring-bound Hobbit from being overwhelmed unless his or her strength is tripled.<br>While this card is in your discard pile, Sméagol is resistance +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("release");

		assertEquals("Release Them", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithSmeagolSpottedItKeepsARingBoundHobbitFromBeingOverwhelmedAndFromTheDiscardPileMakesSmeagolResistancePlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var release = scn.GetFreepsCard("release");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		var brigand1 = scn.GetShadowCard("brigand1");
		// The Shadow player's own copy, in his own discard pile: his Free Peoples card is not active on this turn.
		var shadowRelease = scn.GetShadowCard("release");
		scn.MoveCompanionsToTable(smeagol, sam, merry, boromir);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(release);
		scn.MoveCardsToDiscard(shadowRelease);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1);
		assertInHand(release);
		assertInDiscard(shadowRelease);
		assertEquals(0, scn.GetBurdens());
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(5, scn.GetResistance(sam));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, brigand1);
		scn.FreepsResolveSkirmish(sam);

		// 8 against 3: more than double, less than triple.
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(8, scn.GetStrength(brigand1));
		assertEquals(2, scn.GetOverwhelmMultiplier(sam));
		assertTrue(scn.FreepsPlayAvailable(release));
		scn.FreepsPlayCard(release);

		// The Ring-bound Hobbits, in this skirmish and in none; not an unbound Hobbit, a Hobbit ally, or a
		// Ring-bound companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, bounder, smeagol, boromir, brigand1));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(sam);
		assertEquals(3, scn.GetOverwhelmMultiplier(sam));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));

		// In the discard pile now: Sméagol is resistance +1, and Sam is not.
		assertInDiscard(release);
		assertEquals(7, scn.GetResistance(smeagol));
		assertEquals(5, scn.GetResistance(sam));

		// Sam loses and is not overwhelmed: the one wound of a lost skirmish.
		scn.PassSkirmishActions();
		assertInZone(Zone.FREE_CHARACTERS, sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithoutSmeagolInPlayItCannotBePlayedThoughGollumIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var release = scn.GetFreepsCard("release");
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCompanionsToTable(sam);
		// Sméagol in hand, not in play.
		scn.MoveCardsToHand(release, smeagol);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		assertInHand(release);
		assertInHand(smeagol);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, gollum);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.IsCharSkirmishingAgainst(sam, gollum));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(release));
	}

	@Test
	public void TheShireSmeagolIsSpottedTooAndAHobbitBeatenByTripleHisStrengthIsOverwhelmedAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var release = scn.GetFreepsCard("release");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var sam = scn.GetFreepsCard("sam");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToHand(release);

		scn.StartGame();
		scn.MoveMinionsToTable(party);
		assertInHand(release);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, party);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(3, scn.GetStrength(sam));
		assertEquals(9, scn.GetStrength(party));
		assertTrue(scn.FreepsPlayAvailable(release));
		scn.FreepsPlayCard(release);
		// Frodo and Sam are the Hobbits to choose from.
		scn.FreepsChooseCard(sam);
		assertEquals(3, scn.GetOverwhelmMultiplier(sam));

		// 9 is three times 3: overwhelmed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnlyTheHobbitChosenIsProtectedAndOnlyForTheSkirmishPhaseItWasPlayedIn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var release = scn.GetFreepsCard("release");
		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(smeagol, sam, aragorn);
		scn.MoveCardsToHand(release);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, brigand1);
		assertInHand(release);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{sam, brigand1});
		scn.FreepsResolveSkirmish(aragorn);

		// Sam, who waits for his own skirmish, is the Hobbit chosen during Aragorn's; Frodo is not.
		assertEquals(2, scn.GetOverwhelmMultiplier(sam));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));
		scn.FreepsPlayCard(release);
		scn.FreepsChooseCard(sam);
		assertEquals(3, scn.GetOverwhelmMultiplier(sam));
		assertEquals(2, scn.GetOverwhelmMultiplier(frodo));

		// In the next skirmish phase the protection is gone: Sam, 3 against 8, is overwhelmed.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(2, scn.GetOverwhelmMultiplier(sam));
		scn.FreepsResolveSkirmish(sam);
		assertTrue(scn.IsCharSkirmishingAgainst(sam, brigand1));
		assertEquals(2, scn.GetOverwhelmMultiplier(sam));
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoRingBoundHobbitInPlayItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoHobbitScenario();

		var release = scn.GetFreepsCard("release");
		var smeagol = scn.GetFreepsCard("smeagol");
		var merry = scn.GetFreepsCard("merry");
		var brigand1 = scn.GetShadowCard("brigand1");
		scn.MoveCompanionsToTable(smeagol, merry);
		scn.MoveCardsToHand(release);

		scn.StartGame();
		scn.MoveMinionsToTable(brigand1);
		assertInHand(release);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, brigand1);
		scn.FreepsResolveSkirmish(merry);

		// An event's target is no requirement: with Sméagol spotted and no Ring-bound Hobbit, it is still played.
		assertTrue(scn.FreepsPlayAvailable(release));
		scn.FreepsPlayCard(release);
		assertInDiscard(release);
		assertEquals(2, scn.GetOverwhelmMultiplier(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Merry, an unbound Hobbit, is overwhelmed by 8 against 3.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, merry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoCopiesInTheDiscardPileTheShireSmeagolIsResistancePlusTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var release = scn.GetFreepsCard("release");
		var release2 = scn.GetFreepsCard("release2");
		var smeagol = scn.GetFreepsCard("shiresmeagol");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(smeagol, sam);
		scn.MoveCardsToHand(release, release2);

		scn.StartGame();
		assertInHand(release);
		assertInHand(release2);
		assertEquals(6, scn.GetResistance(smeagol));
		assertEquals(5, scn.GetResistance(sam));

		scn.MoveCardsToDiscard(release);
		assertEquals(7, scn.GetResistance(smeagol));

		// Each copy's own text: +1 for each.
		scn.MoveCardsToDiscard(release2);
		assertInDiscard(release, release2);
		assertEquals(8, scn.GetResistance(smeagol));
		assertEquals(5, scn.GetResistance(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
