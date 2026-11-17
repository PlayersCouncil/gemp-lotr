package com.gempukku.lotro.cards.unofficial.rtmd.set_94_pc;

import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertAttachedTo;
import static org.junit.Assert.*;

public class Card_94_020_Tests
{
	protected VirtualTableScenario GetFreepsScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "4_310");     // Merry, Learned Guide: "Skirmish: Exert the Ring-bearer to discard Merry", whoever is skirmishing
					put("resistance", "2_79"); // Resistance Becomes Unbearable: Shadow Maneuver event, exert a twilight Nazgul to exert the Ring-bearer
					put("twigul", "2_82");     // Ulaire Attea, The Easterling: twilight Nazgul, vitality 3; pays for Resistance Becomes Unbearable
					put("troop", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4; fights the Troop so that there is a skirmish
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound and no Ring-bearer, vitality 4; a fellowship ability used only on purpose
					put("isildur", "18_54");   // Isildur, Sword-bearer: strength 7, vitality 3; "Each time Isildur loses a skirmish, exert a companion."
					put("friends", "2_98");    // Dear Friends: Fellowship event, cost 0, "Exert a companion to place a Free Peoples card from your discard pile beneath your draw deck"
					put("sting", "1_313");     // Sting: [shire] possession, bearer must be Frodo; "Fellowship or Regroup: Exert Frodo to reveal an opponent's hand. Remove (1) for each Orc revealed"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				"94_20", null
		);
	}

	protected VirtualTableScenario GetShadowScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "4_310");     // Merry, Learned Guide: "Skirmish: Exert the Ring-bearer to discard Merry", whoever is skirmishing
					put("resistance", "2_79"); // Resistance Becomes Unbearable: Shadow Maneuver event, exert a twilight Nazgul to exert the Ring-bearer
					put("twigul", "2_82");     // Ulaire Attea, The Easterling: twilight Nazgul, vitality 3; pays for Resistance Becomes Unbearable
					put("troop", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4; fights the Troop so that there is a skirmish
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound and no Ring-bearer, vitality 4; a fellowship ability used only on purpose
					put("isildur", "18_54");   // Isildur, Sword-bearer: strength 7, vitality 3; "Each time Isildur loses a skirmish, exert a companion."
					put("friends", "2_98");    // Dear Friends: Fellowship event, cost 0, "Exert a companion to place a Free Peoples card from your discard pile beneath your draw deck"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing,
				null, "94_20"
		);
	}

	// A Ring-bearer who is not Frodo: Galadriel, Bearer of Wisdom, whose own text is about her resistance alone.
	protected VirtualTableScenario GetGaladrielScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "4_310");     // Merry, Learned Guide: "Skirmish: Exert the Ring-bearer to discard Merry", whoever is skirmishing
					put("resistance", "2_79"); // Resistance Becomes Unbearable: Shadow Maneuver event, exert a twilight Nazgul to exert the Ring-bearer
					put("twigul", "2_82");     // Ulaire Attea, The Easterling: twilight Nazgul, vitality 3; pays for Resistance Becomes Unbearable
					put("troop", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4; fights the Troop so that there is a skirmish
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound and no Ring-bearer, vitality 4; a fellowship ability used only on purpose
					put("isildur", "18_54");   // Isildur, Sword-bearer: strength 7, vitality 3; "Each time Isildur loses a skirmish, exert a companion."
					put("friends", "2_98");    // Dear Friends: Fellowship event, cost 0, "Exert a companion to place a Free Peoples card from your discard pile beneath your draw deck"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.GaladrielRB,
				VirtualTableScenario.RulingRing,
				"94_20", null
		);
	}

	protected VirtualTableScenario GetControlScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "4_310");     // Merry, Learned Guide: "Skirmish: Exert the Ring-bearer to discard Merry", whoever is skirmishing
					put("resistance", "2_79"); // Resistance Becomes Unbearable: Shadow Maneuver event, exert a twilight Nazgul to exert the Ring-bearer
					put("twigul", "2_82");     // Ulaire Attea, The Easterling: twilight Nazgul, vitality 3; pays for Resistance Becomes Unbearable
					put("troop", "1_177");     // Goblin Patrol Troop: strength 13, no text
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4; fights the Troop so that there is a skirmish
					put("sam", "1_311");       // Sam, Son of Hamfast: Ring-bound and no Ring-bearer, vitality 4; a fellowship ability used only on purpose
					put("isildur", "18_54");   // Isildur, Sword-bearer: strength 7, vitality 3; "Each time Isildur loses a skirmish, exert a companion."
					put("friends", "2_98");    // Dear Friends: Fellowship event, cost 0, "Exert a companion to place a Free Peoples card from your discard pile beneath your draw deck"
					put("sting", "1_313");     // Sting: [shire] possession, bearer must be Frodo; "Fellowship or Regroup: Exert Frodo to reveal an opponent's hand. Remove (1) for each Orc revealed"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StatsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {
		/**
		 * Set: RTMD 94
		 * Name: Race Text 94_20
		 * Type: MetaSite
		 * Intensity: 3
		 * Game Text: Your Ring-bearer may not be exerted by Free Peoples cards.
		 */
		var scn = GetFreepsScenario();
		var mod = scn.GetFreepsCard("mod");
		assertEquals("Race Text 94_20", mod.getBlueprint().getTitle());
		assertEquals(CardType.METASITE, mod.getBlueprint().getCardType());
		assertEquals(3, mod.getBlueprint().getIntensity());
	}

	@Test
	public void FreepsCardCannotExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		// Merry's own "Skirmish: exert the Ring-bearer" action has no legal cost target
		assertFalse(scn.FreepsActionAvailable(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithoutTheModFreepsCardsCanExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		assertTrue(scn.FreepsActionAvailable(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void ShadowSourcedExertionStillWorks() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var resistance = scn.GetShadowCard("resistance");
		var twigul = scn.GetShadowCard("twigul");
		var frodo = scn.GetRingBearer();

		scn.MoveMinionsToTable(twigul);
		scn.MoveCardsToHand(resistance);
		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(twigul));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(resistance));
		scn.ShadowPlayCard(resistance);

		// Cost (exert the Nazgul) paid, and the Ring-bearer exert -- sourced from the Shadow
		// event, not a Free Peoples card -- goes through despite the modifier.
		assertEquals(1, scn.GetWoundsOn(twigul));
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ScopingWhenShadowOwnsModFreepsRingBearerIsUnprotected() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");

		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		// requires: OwnerIsFreeps is false here, so the block is inactive
		assertTrue(scn.FreepsActionAvailable(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnEventsExertionMayBePaidByAnyCompanionButTheRingBearerWhoIsNotFrodo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetGaladrielScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var friends = scn.GetFreepsCard("friends");
		var galadriel = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(friends);
		scn.StartGame();

		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertTrue(scn.FreepsPlayAvailable(friends));
		scn.FreepsPlayCard(friends);

		// "Exert a companion": Sam is Ring-bound but bears no Ring, and may pay like Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(galadriel));
		scn.FreepsChooseCard(sam);

		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(galadriel));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ACompanionsTextThatExertsACompanionDoesNotOfferTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var isildur = scn.GetFreepsCard("isildur");
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(isildur, aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(isildur, troop);
		scn.PassCurrentPhaseActions();
		// The wound for losing and Isildur's own trigger come at once: the wound first.
		scn.FreepsResolveRuleFirst();

		// Isildur (7) loses to the Troop (13) and takes a wound; his own text then exerts a companion.
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsHasCardChoiceAvailable(isildur, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo));
		scn.FreepsChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(isildur));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASitesTextMayStillExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var aragorn = scn.GetFreepsCard("aragorn");
		var troop = scn.GetShadowCard("troop");
		var frodo = scn.GetRingBearer();

		scn.MoveCompanionsToTable(aragorn);
		scn.MoveMinionsToTable(troop);
		scn.StartGame();
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);

		// Ettenmoors, site 2: "Skirmish: Exert your companion or minion to make that character strength +2."
		// A site is no Free Peoples card.
		var ettenmoors = scn.GetCurrentSite();
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(ettenmoors));
		scn.FreepsUseCardAction(ettenmoors);

		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheOwnerInPlayerTwosSeatCannotExertHisRingBearerWithHisOwnEventOnHisOwnTurn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetShadowScenario();
		var aragorn = scn.GetShadowCard("aragorn");
		var sam = scn.GetShadowCard("sam");
		var friends = scn.GetShadowCard("friends");
		var frodo = scn.GetShadowRingBearer();

		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToHand(friends);
		scn.StartGame();

		// Player 2's turn: he owns the modifier and is now the Free Peoples player.
		scn.SkipToOtherPlayersTurn();

		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowPlayAvailable(friends));
		scn.ShadowPlayCard(friends);

		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(aragorn);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.ShadowDecisionAvailable("Play Fellowship action or Pass"));
	}

	@Test
	public void APossessionCannotExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFreepsScenario();
		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, sting);
		assertAttachedTo(sting, frodo);
		scn.StartGame();

		// Sting's own "Fellowship: Exert Frodo to ..." has its bearer, the Ring-bearer, for its only cost, and he
		// is unwounded: a Free Peoples possession, so no legal payment.
		assertAttachedTo(sting, frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(sting));
	}

	@Test
	public void WithoutTheModAPossessionCanExertTheRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetControlScenario();
		var sting = scn.GetFreepsCard("sting");
		var frodo = scn.GetRingBearer();

		scn.AttachCardsTo(frodo, sting);
		assertAttachedTo(sting, frodo);
		scn.StartGame();

		assertAttachedTo(sting, frodo);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(0, scn.GetShadowHandCount());
		assertTrue(scn.FreepsActionAvailable(sting));
		scn.FreepsUseCardAction(sting);

		// The Shadow player's hand is empty: nothing is revealed, and Frodo's exertion is all that is done.
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
