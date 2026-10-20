package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_069_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("noman", "103_69");
					put("noman2", "103_69");
					put("witchking", "1_237");     // The Witch-king, Lord of Angmar: vitality 4; fierce, strength +2 for each other Nazgul
					put("twilightking", "2_85");   // The Witch-king, Lord of the Nazgul: vitality 4; twilight, and a trigger on winning a skirmish
					put("enquea", "1_231");        // Ulaire Enquea, Lieutenant of Morgul: Nazgul, vitality 4; his maneuver ability needs 6 companions or 5 burdens
					put("attea", "103_61");        // Ulaire Attea, Khamul of the East: the one Nazgul who is not [wraith] ([raider]); his text needs 3 burdens
					put("rider", "12_161");        // Black Rider: [wraith] Nazgul, vitality 3; fierce, strength +1 for each companion
					put("otsea", "1_235");         // Ulaire Otsea, Lieutenant of Morgul: Nazgul, strength 9, not fierce; a Shadow-phase ability
					put("lemenya", "1_232");       // Ulaire Lemenya, Lieutenant of Morgul: Nazgul, strength 9, not fierce; weakens bearers of [wraith] conditions
					put("hound", "7_192");         // Morgul Hound: [wraith] Orc; its text needs 2 wounds on the Ring-bearer
					put("nazgulsword", "1_218");   // Nazgul Sword: Shadow possession a Nazgul bears; damage +1 only at 3 burdens
					put("poison", "8_28");         // Spider Poison: Shadow possession a companion bears; bearer exerts when assigned to a skirmish

					put("aragorn", "1_89");        // Aragorn, Ranger of the North: strength 8
					put("rangersword", "1_112");   // Ranger's Sword: [gondor] possession on Aragorn
					put("hobbitsword", "1_299");   // Hobbit Sword: [shire] possession on a Hobbit; no text
					put("leaf", "1_300");          // Longbottom Leaf: [shire] possession in the support area; a when-played trigger only
					put("shards", "3_44");         // The Shards of Narsil: Free Peoples artifact in the support area; fellowship abilities only
					put("lordofmoria", "1_21");    // Lord of Moria: Free Peoples condition in the support area; each Dwarf is damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NoManMayHinderMeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: No Man May Hinder Me
		 * Unique: false
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver or Skirmish
		 * Game Text: <b>Maneuver</b> <i>or</i> <b>Skirmish</b>: Restore a Nazgul. Then you may exert The Witch-king
		 * 	twice to hinder all Free Peoples possessions.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("noman");

		assertEquals("No Man May Hinder Me", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RestoresOneHinderedNazgulOfThePlayersChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var rider = scn.GetShadowCard("rider");
		var attea = scn.GetShadowCard("attea");
		var hound = scn.GetShadowCard("hound");
		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(noman);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, attea, hound, enquea);
		// Hindered: a [wraith] Nazgul, the [raider] Nazgul, a [wraith] Orc and a companion. Enquea is a Nazgul who is not hindered.
		scn.HinderCard(rider, attea, hound, aragorn);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.SetTwilight(3);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(noman));
		scn.ShadowPlayCard(noman);

		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, attea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(hound, enquea, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(attea);

		assertFalse(scn.IsHindered(attea));
		assertTrue(scn.IsHindered(rider));
		assertTrue(scn.IsHindered(hound));
		assertTrue(scn.IsHindered(aragorn));
		assertInDiscard(noman);

		// No Witch-king is in play, so nothing more is asked: Enquea, with vitality 4, is not him.
		assertFalse(scn.ShadowDecisionAvailable("exert The Witch-king twice"));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CanBePlayedInTheManeuverAndSkirmishPhasesAndInNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var noman2 = scn.GetShadowCard("noman2");
		var otsea = scn.GetShadowCard("otsea");
		var lemenya = scn.GetShadowCard("lemenya");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(noman, noman2);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, lemenya);
		scn.HinderCard(otsea);
		scn.SetTwilight(20);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(noman));
		scn.ShadowPass();

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(noman));
		scn.ShadowPass();

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(noman));
		scn.ShadowPass();

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(noman));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(noman));

		// Played in the skirmish phase: Otsea is the only hindered Nazgul and is restored without a choice.
		assertTrue(scn.IsHindered(otsea));
		scn.ShadowPlayCard(noman);
		assertFalse(scn.IsHindered(otsea));
		assertInDiscard(noman);

		scn.PassSkirmishActions();

		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(noman2));
	}

	@Test
	public void TheWitchKingMayThenExertTwiceToHinderEveryFreePeoplesPossessionAndNothingElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var enquea = scn.GetShadowCard("enquea");
		var rider = scn.GetShadowCard("rider");
		var nazgulsword = scn.GetShadowCard("nazgulsword");
		var poison = scn.GetShadowCard("poison");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		var leaf = scn.GetFreepsCard("leaf");
		var shards = scn.GetFreepsCard("shards");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword, poison);
		scn.AttachCardsTo(frodo, hobbitsword);
		scn.MoveCardsToSupportArea(leaf, shards, lordofmoria);
		scn.MoveCardsToHand(noman);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, enquea, rider);
		scn.AttachCardsTo(enquea, nazgulsword);
		scn.HinderCard(rider);
		// Vitality 4 with 1 wound: exactly the two exertions asked for.
		scn.AddWoundsToChar(witchking, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(noman);
		// The Rider is the only hindered Nazgul and is restored without a choice.
		assertFalse(scn.IsHindered(rider));

		assertTrue(scn.ShadowDecisionAvailable("exert The Witch-king twice"));
		scn.ShadowChooseYes();

		assertEquals(3, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertEquals(0, scn.GetWoundsOn(rider));

		// Possessions of two cultures, borne by the Ring-bearer, borne by another companion and in the support area.
		assertTrue(scn.IsHindered(rangersword));
		assertTrue(scn.IsHindered(hobbitsword));
		assertTrue(scn.IsHindered(leaf));
		// A Free Peoples artifact and condition, the Shadow possessions on a companion and on a minion, and the bearers.
		assertFalse(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(lordofmoria));
		assertFalse(scn.IsHindered(poison));
		assertFalse(scn.IsHindered(nazgulsword));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(frodo));

		assertInDiscard(noman);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoNazgulToRestoreTheOfferIsStillMadeAndDecliningExertsNobodyAndHindersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToHand(noman);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertFalse(scn.IsHindered(witchking));
		assertTrue(scn.ShadowPlayAvailable(noman));
		scn.ShadowPlayCard(noman);

		assertTrue(scn.ShadowDecisionAvailable("exert The Witch-king twice"));
		scn.ShadowChooseNo();

		assertEquals(0, scn.GetWoundsOn(witchking));
		assertFalse(scn.IsHindered(rangersword));
		assertInDiscard(noman);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheOfferIsNotMadeWhenTheWitchKingCanExertOnlyOnceThoughAnotherNazgulCouldExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var enquea = scn.GetShadowCard("enquea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rangersword = scn.GetFreepsCard("rangersword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, rangersword);
		scn.MoveCardsToHand(noman);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, enquea);
		// Vitality 4 with 2 wounds: one exertion left. Enquea has vitality 4 and no wound.
		scn.AddWoundsToChar(witchking, 2);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(noman);

		assertFalse(scn.ShadowDecisionAvailable("exert The Witch-king twice"));
		assertEquals(2, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(enquea));
		assertFalse(scn.IsHindered(rangersword));
		assertInDiscard(noman);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AWitchKingRestoredByTheEventCanThenExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var twilightking = scn.GetShadowCard("twilightking");
		var attea = scn.GetShadowCard("attea");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.AttachCardsTo(frodo, hobbitsword);
		scn.MoveCardsToHand(noman);

		scn.StartGame();
		// The Hound is there so that a minion that is not hindered is in play.
		scn.MoveMinionsToTable(twilightking, attea, hound);
		scn.HinderCard(twilightking, attea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(noman);
		assertTrue(scn.ShadowHasCardChoiceAvailable(twilightking, attea));
		scn.ShadowChooseCard(twilightking);
		assertFalse(scn.IsHindered(twilightking));
		assertTrue(scn.IsHindered(attea));

		assertTrue(scn.ShadowDecisionAvailable("exert The Witch-king twice"));
		scn.ShadowChooseYes();

		assertEquals(2, scn.GetWoundsOn(twilightking));
		assertTrue(scn.IsHindered(hobbitsword));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AWitchKingLeftHinderedIsNotAskedToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var rider = scn.GetShadowCard("rider");
		var hound = scn.GetShadowCard("hound");
		var frodo = scn.GetRingBearer();
		var hobbitsword = scn.GetFreepsCard("hobbitsword");
		scn.AttachCardsTo(frodo, hobbitsword);
		scn.MoveCardsToHand(noman);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, rider, hound);
		scn.HinderCard(witchking, rider);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(noman);
		assertTrue(scn.ShadowHasCardChoiceAvailable(witchking, rider));
		scn.ShadowChooseCard(rider);
		assertFalse(scn.IsHindered(rider));
		assertTrue(scn.IsHindered(witchking));

		// A hindered character cannot be exerted.
		assertFalse(scn.ShadowDecisionAvailable("exert The Witch-king twice"));
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertFalse(scn.IsHindered(hobbitsword));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void WithNoFreePeoplesPossessionInPlayTheExertionIsStillOfferedAndPaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var noman = scn.GetShadowCard("noman");
		var witchking = scn.GetShadowCard("witchking");
		var shards = scn.GetFreepsCard("shards");
		var lordofmoria = scn.GetFreepsCard("lordofmoria");
		scn.MoveCardsToSupportArea(shards, lordofmoria);
		scn.MoveCardsToHand(noman);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		scn.ShadowPlayCard(noman);

		// The exertion is a cost that can be paid, so the question is asked though nothing would be hindered.
		assertTrue(scn.ShadowDecisionAvailable("exert The Witch-king twice"));
		scn.ShadowChooseYes();

		assertEquals(2, scn.GetWoundsOn(witchking));
		assertFalse(scn.IsHindered(shards));
		assertFalse(scn.IsHindered(lordofmoria));
		assertInDiscard(noman);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
