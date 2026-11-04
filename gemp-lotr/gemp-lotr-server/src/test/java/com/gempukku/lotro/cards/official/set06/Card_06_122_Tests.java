package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_122_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("witchking", "6_122");

					put("khamul", "103_61");    // Úlairë Attëa, Khamûl of the East: a [raider] Nazgûl, strength 12, fierce; his bonuses need 3 burdens
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc and no Nazgûl, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer
					// Orkish Skirmisher: [orc] Orc, strength 9, vitality 3; each time he wins a skirmish he may exert twice to wound a companion
					put("skirmisher", "18_90");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit companion, strength 3, vitality 4; his response needs Frodo to die
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf companion, vitality 3; a skirmish ability used only on purpose
					put("smeagol", "7_71");     // Sméagol, Always Helps: [gollum] companion, strength 3; his assignment ability is used only on purpose
					put("deadcity", "7_56");    // The Dead City: on Sméagol, "if Sméagol is about to be killed in a skirmish, he is discarded instead"
					put("grimir", "1_17");      // Grimir, Dwarven Elder: [dwarven] Dwarf ally, vitality 3; a fellowship ability used only on purpose
					put("barliman", "1_70");    // Barliman Butterbur: [gandalf] Man ally, vitality 2; a fellowship ability used only on purpose
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: [dwarven] Dwarf ally, strength 4, vitality 2; takes part in skirmishes by his own maneuver ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TheWitchkingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: The Witch-king, Deathless Lord
		 * Unique: True
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 8
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 14
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.<br>Each time a companion is killed in a skirmish involving a Nazgûl, wound an ally twice or exert a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("witchking");

		assertEquals("The Witch-king", card.getBlueprint().getTitle());
		assertEquals("Deathless Lord", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(14, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachCompanionKilledInASkirmishInvolvingANazgulIsAnsweredByTheBranchTheShadowPlayerChooses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var khamul = scn.GetShadowCard("khamul");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);
		scn.MoveAlliesToTable(grimir, barliman);

		scn.StartGame();
		// Aragorn has one vitality left: the wound for losing kills him.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(witchking, khamul);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, khamul}, new PhysicalCardImpl[]{sam, witchking});

		// A [raider] Nazgûl beats a [gondor] Man, 12 against 8, without overwhelming him: the wound kills him.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);

		// Both can be done, so the Shadow player is asked which.
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowChoiceAvailable("Wound an ally twice"));
		assertTrue(scn.ShadowChoiceAvailable("Exert a companion"));
		scn.ShadowChooseOption("Wound an ally twice");
		// Allies of two cultures; no companion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(grimir, barliman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, gimli));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(grimir);
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// The Witch-king overwhelms a [shire] Hobbit, 14 against 3: a second kill, answered again.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertEquals(2, scn.ShadowGetMultipleChoiceCount());
		scn.ShadowChooseOption("Exert a companion");
		// Companions of two cultures; no ally, no minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(grimir, barliman, witchking, khamul));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(barliman));

		// Both minions are fierce.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithNoAllyACompanionIsExertedWithNoChoiceOfBranchAndAnExhaustedOneIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);

		scn.StartGame();
		// Aragorn is exhausted and stays out of the skirmishes.
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(witchking);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, witchking);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);

		// No ally to wound: no choice of branch, and the exhausted companion is not offered for the exertion.
		assertEquals(0, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(aragorn, witchking));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithEveryCompanionExhaustedAnAllyIsWoundedTwiceWithNoChoiceOfBranch() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		var barliman = scn.GetFreepsCard("barliman");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(sam, gimli);
		scn.MoveAlliesToTable(grimir, barliman, thrarin);

		scn.StartGame();
		// Frodo and Gimli are exhausted; Thrarin has one vitality left, too few for two wounds.
		scn.AddWoundsToChar(frodo, 3);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(thrarin, 1);
		scn.MoveMinionsToTable(witchking);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(thrarin));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, witchking);
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);

		// No companion can exert: no choice of branch.  Only an ally who can take both wounds is offered.
		assertEquals(0, scn.ShadowGetMultipleChoiceCount());
		assertTrue(scn.ShadowHasCardChoiceAvailable(grimir, barliman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(thrarin, frodo, gimli));
		scn.ShadowChooseCard(barliman);
		// Vitality 2 and two wounds: wounds, not exertions, and he is killed.
		assertInZone(Zone.DEAD, barliman);
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ACompanionInNoSkirmishKilledDuringTheWitchkingsSkirmishIsAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var skirmisher = scn.GetShadowCard("skirmisher");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);

		scn.StartGame();
		// Sam has one vitality left and will be in no skirmish.
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(witchking, skirmisher);
		// Aragorn at 12 is beaten by 14 + 9 without being overwhelmed (23 is less than 24).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, witchking);
		scn.ShadowAssignToMinions(aragorn, skirmisher);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Skirmisher's win, in the skirmish phase of the Witch-king's skirmish: he exerts twice to wound Sam, who dies.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(skirmisher));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(sam);
		assertInZone(Zone.DEAD, sam);

		// No ally: the exertion, with no choice of branch.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gimli, aragorn));
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ACompanionKilledInAnotherMinionsSkirmishWhileTheWitchkingWaitsForHisIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var hound = scn.GetShadowCard("hound");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, sam, gimli);

		scn.StartGame();
		// Aragorn and Sam each have one vitality left.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(sam, 3);
		scn.MoveMinionsToTable(witchking, hound);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(sam));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, witchking}, new PhysicalCardImpl[]{sam, hound});

		// The [wraith] Orc beats Sam, 5 against 3, while the Witch-king is assigned and waiting: no Nazgûl is in that skirmish.
		scn.FreepsResolveSkirmish(sam);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// The control on the same board: the Witch-king's own skirmish kills Aragorn, and is answered.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, gimli));
		scn.ShadowChooseCard(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ACompanionDiscardedInsteadOfKilledInTheWitchkingsSkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var smeagol = scn.GetFreepsCard("smeagol");
		var deadcity = scn.GetFreepsCard("deadcity");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(smeagol, gimli);
		scn.MoveAlliesToTable(grimir);
		scn.AttachCardsTo(smeagol, deadcity);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		assertAttachedTo(deadcity, smeagol);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, witchking);

		// 14 against 3 overwhelms Sméagol, and The Dead City has him discarded instead of killed.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		assertInDiscard(smeagol);
		assertNotInZone(Zone.DEAD, smeagol);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAllyKilledInTheWitchkingsSkirmishIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var witchking = scn.GetShadowCard("witchking");
		var thrarin = scn.GetFreepsCard("thrarin");
		var gimli = scn.GetFreepsCard("gimli");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin, grimir);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, witchking);

		// 14 against 4: the ally is overwhelmed and killed in the Witch-king's skirmish, and he is no companion.
		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, thrarin);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}
}
