package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V1_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("faces", "101_43");
					put("faces2", "101_43");

					// Twilight Nazgul
					put("attea", "2_82");       // Ulaire Attea, The Easterling: unique, strength 12; its text is for when it is played
					put("rit", "101_40");       // Ringwraith in Twilight: not unique, strength 9, and +1 of its own for each wound on the Ring-bearer
					put("enquea", "2_83");      // Ulaire Enquea, Ringwraith in Twilight: unique, strength 11; its text is for when it wins
					put("wk", "2_85");          // The Witch-king, Lord of the Nazgul: strength 14, vitality 4; may exert when he wins a skirmish

					put("lemenya", "1_232");    // Ulaire Lemenya, Lieutenant of Morgul: a Nazgul that is not twilight, strength 9
					put("lights", "6_99");      // Corpse Lights: a twilight [sauron] Wraith, strength 7; a skirmish ability that is never used

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhiteFacesBurnedStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V1
		 * Name: White Faces Burned
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make a skirmishing twilight Nazgul strength +1 for each wound on the Ring- bearer (and damage +1 if that Nazgul is The Witch-king).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("faces");

		assertEquals("White Faces Burned", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesATwilightNazgulStrengthPlusOneForEachWoundOnTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var faces = scn.GetShadowCard("faces");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(faces);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		// What is counted is the wounds on the Ring-bearer: 3 of them, beside 2 burdens on him and 2 wounds
		// on Aragorn.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddBurdens(2);
		scn.AddWoundsToChar(aragorn, 2);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(12, scn.GetStrength(attea));
		// 2 companions + 2 for the shadow number of site 2
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.HAND, faces);
		assertTrue(scn.ShadowPlayAvailable(faces));

		scn.ShadowPlayCard(faces);

		// 12 + 3 wounds on the Ring-bearer
		assertEquals(15, scn.GetStrength(attea));
		assertFalse(scn.HasKeyword(attea, Keyword.DAMAGE));
		assertInDiscard(faces);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnlyATwilightNazgulInTheSkirmishMayBeChosenAndOnlyTheOneChosenIsStronger() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var faces = scn.GetShadowCard("faces");
		var attea = scn.GetShadowCard("attea");
		var rit = scn.GetShadowCard("rit");
		var enquea = scn.GetShadowCard("enquea");
		var lemenya = scn.GetShadowCard("lemenya");
		var lights = scn.GetShadowCard("lights");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(faces);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.MoveMinionsToTable(attea, enquea, lemenya, lights);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		// Put down here, past the offer it makes at the start of the maneuver phase.
		scn.MoveMinionsToTable(rit);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, attea }, new PhysicalCardImpl[] { gimli, enquea });
		scn.ShadowAssignToMinions(aragorn, rit, lemenya, lights);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(attea));
		// 9 + 1 by its own text for the wound on the Ring-bearer
		assertEquals(10, scn.GetStrength(rit));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(lights));
		assertEquals(11, scn.GetStrength(enquea));

		scn.ShadowPlayCard(faces);

		// In Aragorn's skirmish: a unique twilight Nazgul, and one that is not unique.
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, rit));
		// In Aragorn's skirmish: a Nazgul that is not twilight, and a twilight minion that is not a Nazgul.
		// Assigned to Gimli, whose skirmish has not begun: a twilight Nazgul.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, lights, enquea));
		scn.ShadowChooseCard(rit);

		// 9 + 1 by its own text + 1 from the event
		assertEquals(11, scn.GetStrength(rit));
		assertEquals(12, scn.GetStrength(attea));
		assertEquals(9, scn.GetStrength(lemenya));
		assertEquals(7, scn.GetStrength(lights));
		assertEquals(11, scn.GetStrength(enquea));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void IsPlayedInASkirmishAndNotInTheAssignmentPhaseBeforeNorTheRegroupPhaseAfter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var faces = scn.GetShadowCard("faces");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(faces);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.AddWoundsToChar(frodo, 1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		// 2 companions + 2 for the shadow number of site 2: the pool would pay for it
		assertEquals(4, scn.GetTwilight());
		assertFalse(scn.ShadowPlayAvailable(faces));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(faces));

		// Attea's 12 beats Aragorn's 8; Attea is not fierce, so the regroup phase follows.
		scn.ShadowPass();
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertInZone(Zone.SHADOW_CHARACTERS, attea);
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.HAND, faces);
		assertFalse(scn.ShadowPlayAvailable(faces));
	}

	@Test
	public void StrengthAndDamageBonusesLastThroughThatSkirmishAndAreGoneInTheNext() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var faces = scn.GetShadowCard("faces");
		var wk = scn.GetShadowCard("wk");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(faces);

		scn.StartGame();
		scn.MoveMinionsToTable(wk, lemenya);
		scn.AddWoundsToChar(frodo, 1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, wk }, new PhysicalCardImpl[] { gimli, lemenya });
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(14, scn.GetStrength(wk));
		assertFalse(scn.HasKeyword(wk, Keyword.DAMAGE));

		scn.ShadowPlayCard(faces);
		// 14 + 1 wound on the Ring-bearer
		assertEquals(15, scn.GetStrength(wk));
		assertEquals(1, scn.GetKeywordCount(wk, Keyword.DAMAGE));

		// Both players have acted again and the skirmish is about to be resolved: both are still there.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(15, scn.GetStrength(wk));
		assertEquals(1, scn.GetKeywordCount(wk, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPass();
		// 15 against Aragorn's 8: he loses and takes 1 wound + 1 for damage +1.
		// Engine pass 54: what answers the win comes before the loser loses.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The Witch-king's own text: he may exert when he wins a skirmish.
		scn.ShadowDeclineOptionalTrigger();

		// Gimli's skirmish, later in the same turn.
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(14, scn.GetStrength(wk));
		assertFalse(scn.HasKeyword(wk, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void MakesTheWitchKingDamagePlusOneAndNoStrongerWhileTheRingBearerHasNoWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var faces = scn.GetShadowCard("faces");
		var wk = scn.GetShadowCard("wk");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(faces);

		scn.StartGame();
		scn.MoveMinionsToTable(wk);
		// No wound on the Ring-bearer; the burdens on him and the wounds on Aragorn are not counted.
		scn.AddBurdens(2);
		scn.AddWoundsToChar(aragorn, 2);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, wk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(14, scn.GetStrength(wk));
		assertFalse(scn.HasKeyword(wk, Keyword.DAMAGE));
		assertEquals(4, scn.GetTwilight());
		assertTrue(scn.ShadowPlayAvailable(faces));

		scn.ShadowPlayCard(faces);

		assertEquals(14, scn.GetStrength(wk));
		assertTrue(scn.HasKeyword(wk, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(wk, Keyword.DAMAGE));
		assertInDiscard(faces);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnlyTheNazgulChosenIsDamagePlusOneAndOnlyIfItIsTheWitchKing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var faces = scn.GetShadowCard("faces");
		var faces2 = scn.GetShadowCard("faces2");
		var attea = scn.GetShadowCard("attea");
		var wk = scn.GetShadowCard("wk");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(faces, faces2);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		// Two twilight Nazgul in one skirmish: The Witch-king, and one who is not.
		scn.MoveMinionsToTable(attea, wk);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.ShadowAssignToMinions(aragorn, wk);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(attea));
		assertEquals(14, scn.GetStrength(wk));
		assertFalse(scn.HasKeyword(attea, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(wk, Keyword.DAMAGE));

		// The Nazgul that is not The Witch-king, chosen with The Witch-king in the same skirmish: stronger,
		// and nobody is damage +1.
		scn.ShadowPlayCard(faces);
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, wk));
		scn.ShadowChooseCard(attea);
		assertEquals(13, scn.GetStrength(attea));
		assertFalse(scn.HasKeyword(attea, Keyword.DAMAGE));
		assertEquals(14, scn.GetStrength(wk));
		assertFalse(scn.HasKeyword(wk, Keyword.DAMAGE));

		// The second copy on The Witch-king: he is damage +1, and the Nazgul beside him is not.
		scn.FreepsPass();
		scn.ShadowPlayCard(faces2);
		assertTrue(scn.ShadowHasCardChoiceAvailable(attea, wk));
		scn.ShadowChooseCard(wk);
		// 14 + 1 wound on the Ring-bearer
		assertEquals(15, scn.GetStrength(wk));
		assertEquals(1, scn.GetKeywordCount(wk, Keyword.DAMAGE));
		assertEquals(13, scn.GetStrength(attea));
		assertFalse(scn.HasKeyword(attea, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BonusIsCountedWhenTheEventIsPlayedAndDoesNotGrowWhenTheRingBearerIsWoundedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var faces = scn.GetShadowCard("faces");
		var attea = scn.GetShadowCard("attea");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(faces);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.AddWoundsToChar(frodo, 1);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, attea);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(12, scn.GetStrength(attea));
		scn.ShadowPlayCard(faces);
		assertEquals(13, scn.GetStrength(attea));

		// Site 2 is Ettenmoors: "Skirmish: Exert your companion or minion to make that character strength
		// +2."  The Free Peoples player exerts Frodo with it, which puts a second wound on him.
		var ettenmoors = scn.GetCurrentSite();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		scn.FreepsUseCardAction(ettenmoors);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);

		assertEquals(2, scn.GetWoundsOn(frodo));
		// Still 12 + 1: the number was fixed when the event was played.
		assertEquals(13, scn.GetStrength(attea));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
