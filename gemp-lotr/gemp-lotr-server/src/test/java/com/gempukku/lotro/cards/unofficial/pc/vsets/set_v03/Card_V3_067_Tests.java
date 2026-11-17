package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_067_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("illwind", "103_67");
					put("lemenya", "1_232");    // Ulaire Lemenya: [wraith] Nazgul, cost 4; weakens bearers of [wraith] conditions
					put("nelya", "2_84");       // Ulaire Nelya, Ringwraith in Twilight: [wraith] Nazgul, cost 5; nothing else below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: the [raider] Nazgul, cost 6; fierce
					put("witchking", "1_237");  // The Witch-king, Lord of Angmar: cost 8; fierce
					put("beast", "12_184");     // The Witch-king's Beast: a mount for any Nazgul; its text needs The Witch-king as bearer
					put("lackey", "7_193");     // Morgul Lackey: [wraith] Orc, cost 2; a skirmish ability
					put("lackey2", "7_193");
					put("fell", "15_182");      // A Shadow Fell Over Them: Assignment: Spot a Nazgul and remove (4) to play a Nazgul from your discard pile

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4
					put("merry", "1_302");      // Merry, Friend to Sam: vitality 4
					put("gimli", "1_13");       // Gimli, Son of Gloin: vitality 3
					put("silinde", "1_60");     // Silinde, Elf of Mirkwood: an ally, vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IllWindStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Ill Wind
		 * Unique: true
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: The second time you play a Nazgul each Shadow phase, you may hinder a wounded companion.
		 * 	The Free Peoples player may make you exert another companion to prevent this (or wound if you
		 * 	can spot The Witch-king).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("illwind");

		assertEquals("Ill Wind", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedForOneAndAnswersTheSecondNazgulOfTheShadowPhaseButNotTheFirstOrThird() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var lemenya = scn.GetShadowCard("lemenya");
		var lackey = scn.GetShadowCard("lackey");
		var lackey2 = scn.GetShadowCard("lackey2");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToHand(illwind, lemenya, lackey, lackey2, khamul, nelya);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPass();

		// The first Nazgul of the phase, before Ill Wind is even in play.
		scn.ShadowPlayCard(lemenya);
		assertTrue(scn.AwaitingShadowPhaseActions());

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(illwind);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, illwind);

		// The second minion of the phase, but a [wraith] Orc and no Nazgul.
		scn.ShadowPlayCard(lackey);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second Nazgul of the phase; this one is the [raider] Nazgul.
		scn.ShadowPlayCard(khamul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(illwind));
		assertFalse(scn.IsHindered(aragorn));
		scn.ShadowAcceptOptionalTrigger();

		// Aragorn is the only wounded companion and is chosen without asking. The Free Peoples player
		// lets it happen.
		scn.FreepsChooseNo();
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(frodo));

		// Another [wraith] Orc, played once the second Nazgul has been: it is not answered in his place.
		scn.ShadowPlayCard(lackey2);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The third.
		scn.ShadowPlayCard(nelya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayHaveAnotherCompanionExertedToPreventTheHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		var beast = scn.GetShadowCard("beast");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(lemenya, khamul);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		var gimli = scn.GetFreepsCard("gimli");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn, sam, merry, gimli);
		scn.MoveAlliesToTable(silinde);
		scn.AddWoundsToChar(frodo, 1);
		// Vitality 4 with 3 wounds: wounded, and exhausted.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(gimli, 1);
		scn.AddWoundsToChar(silinde, 1);

		scn.StartGame();
		scn.HinderCard(gimli);
		// A Nazgul who is not The Witch-king, on The Witch-king's Beast: nothing here spots The Witch-king.
		scn.MoveMinionsToTable(nelya);
		scn.AttachCardsTo(nelya, beast);
		scn.SetTwilight(40);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowPlayCard(khamul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(illwind));
		scn.ShadowAcceptOptionalTrigger();

		// Wounded companions, the Ring-bearer among them. Not an unwounded companion, not a wounded ally,
		// not a wounded companion who is already hindered.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, merry, gimli, silinde));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(frodo);

		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.FreepsDecisionAvailable("exert another companion to prevent"));
		scn.FreepsChooseYes();

		// The Free Peoples player has decided; which companion exerts is the Shadow player's to choose.
		// Another companion who can exert: not Frodo himself, not the exhausted Aragorn, not the
		// hindered Gimli, not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn, gimli, silinde));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.ShadowChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheShadowPlayerChoosesWhichOtherCompanionIsExerted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(lemenya, khamul);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(aragorn, sam, merry);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		scn.ShadowPlayCard(khamul);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(frodo);
		scn.FreepsChooseYes();

		// "Make you exert": the Shadow player exerts, and so chooses whom.
		assertTrue(scn.ShadowHasCardChoiceAvailable(sam, merry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, aragorn));
		scn.ShadowChooseCard(merry);

		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.IsHindered(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheHinderMayBeDeclinedAndThenNobodyIsAskedAnything() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(lemenya, khamul);

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		scn.ShadowPlayCard(khamul);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(illwind));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(aragorn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNoOtherCompanionAbleToExertTheHinderCannotBePrevented() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(lemenya, khamul);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(silinde);
		scn.AddWoundsToChar(frodo, 1);
		// The only other companion is exhausted; the ally could exert but is no companion.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		scn.ShadowPlayCard(khamul);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		scn.ShadowChooseCard(frodo);

		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.IsHindered(frodo));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(silinde));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithTheWitchKingThePreventionIsAWoundWhichAnExhaustedCompanionMayTake() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var lemenya = scn.GetShadowCard("lemenya");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(lemenya, witchking);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(silinde);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 1);
		// Exhausted: he cannot exert, but he can be wounded.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		scn.ShadowPlayCard(witchking);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(illwind));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, sam));
		scn.ShadowChooseCard(sam);

		assertTrue(scn.FreepsDecisionAvailable("wound another companion to prevent"));
		scn.FreepsChooseYes();
		// Whom to wound is the Shadow player's choice, as whom to exert is. Any other companion, the
		// Ring-bearer and the exhausted Aragorn among them; not Sam himself, not an ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, silinde));
		assertInZone(Zone.FREE_CHARACTERS, aragorn);
		scn.ShadowChooseCard(aragorn);

		// A wound and no exertion: it is his fourth, and kills him.
		assertInZone(Zone.DEAD, aragorn);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsHindered(sam));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachShadowPhaseCountsItsOwnNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var lemenya = scn.GetShadowCard("lemenya");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var witchking = scn.GetShadowCard("witchking");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(lemenya, nelya, khamul, witchking);

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(sam, 1);

		scn.StartGame();
		scn.SetTwilight(40);
		scn.FreepsPass();

		// The first Shadow phase of the turn: its second Nazgul is answered.
		scn.ShadowPlayCard(lemenya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		scn.ShadowPlayCard(nelya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(illwind));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(aragorn);
		scn.FreepsChooseYes();
		// The other companion who exerts, chosen by the Shadow player, may be the Ring-bearer.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.ShadowChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertFalse(scn.IsHindered(aragorn));

		scn.SkipToMovementDecision();
		scn.SetTwilight(40);
		scn.FreepsChooseToMove();
		assertEquals(Phase.SHADOW, scn.GetCurrentPhase());

		// The third Nazgul of the turn, but the first of this Shadow phase.
		scn.ShadowPlayCard(khamul);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second of this Shadow phase: answered again, as the second of the first phase was.
		scn.ShadowPlayCard(witchking);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(illwind));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn, sam));
		scn.ShadowChooseCard(sam);
		// The Witch-king is in play now, so what the Free Peoples player is asked for is a wound. It is
		// refused, and the hinder happens.
		assertTrue(scn.FreepsDecisionAvailable("wound another companion to prevent"));
		scn.FreepsChooseNo();

		assertTrue(scn.IsHindered(sam));
		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(frodo));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheSecondNazgulPlayedInAnAssignmentPhaseIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var fell = scn.GetShadowCard("fell");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		var nelya = scn.GetShadowCard("nelya");
		scn.MoveCardsToSupportArea(illwind, fell);
		scn.MoveCardsToDiscard(khamul, nelya);

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 1);

		scn.StartGame();
		// A Nazgul for A Shadow Fell Over Them to spot.
		scn.MoveMinionsToTable(lemenya);
		scn.SetTwilight(40);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		scn.FreepsPass();
		scn.ShadowUseCardAction(fell);
		scn.ShadowChooseCardBPFromSelection(khamul);
		assertInZone(Zone.SHADOW_CHARACTERS, khamul);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());

		// The second Nazgul played in this assignment phase: no Shadow phase, so no offer.
		scn.FreepsPass();
		scn.ShadowUseCardAction(fell);
		assertInZone(Zone.SHADOW_CHARACTERS, nelya);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhenThereIsNoWoundedCompanionToHinder() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var illwind = scn.GetShadowCard("illwind");
		var lemenya = scn.GetShadowCard("lemenya");
		var khamul = scn.GetShadowCard("khamul");
		scn.MoveCardsToSupportArea(illwind);
		scn.MoveCardsToHand(lemenya, khamul);

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var silinde = scn.GetFreepsCard("silinde");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveAlliesToTable(silinde);
		// A wounded ally, and a wounded companion who is already hindered: neither can be chosen.
		scn.AddWoundsToChar(silinde, 1);
		scn.AddWoundsToChar(gimli, 1);

		scn.StartGame();
		scn.HinderCard(gimli);
		scn.SetTwilight(40);
		scn.FreepsPass();

		scn.ShadowPlayCard(lemenya);
		scn.ShadowPlayCard(khamul);

		// No cost and nothing it could do: not offered.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.FreepsAnyDecisionsAvailable());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
