package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_022_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("reclaim", "10_22");
					put("reclaim2", "10_22");

					// The Gollums, and a [gollum] minion who is none.
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: [gollum] minion, vitality 4; a regroup ability only
					put("wraith", "103_66");    // Gollum, Half a Wraith Himself: [wraith] minion, vitality 4; his own assignment ability wants a threat or a companion of cost 0
					put("shelob", "8_26");      // Shelob, Last Child of Ungoliant: [gollum] minion, vitality 8; damage +1 only in a fierce skirmish

					// Companions and what they bear.
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man; a maneuver ability only
					put("barahir", "6_55");     // Ring of Barahir: [gondor] artifact on Aragorn; a start-of-turn trigger, so attached after the start
					put("gimli", "1_13");       // Gimli, Son of Gloin: [dwarven] Dwarf; a skirmish ability only
					put("retribution", "9_9");  // Ring of Retribution: [dwarven] artifact on a Dwarf; a response to his win only
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: [gandalf] Wizard; a strength bonus only
					put("glamdring", "1_75");   // Glamdring: a [gandalf] possession, hand weapon, on Gandalf; an ability used only on purpose
					put("merchant", "7_97");    // Gondorian Merchant: [gondor] Man ally; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: [gondor] artifact on a [gondor] Man; a response to a wound
					put("follow", "1_109");     // One Whom Men Would Follow: Maneuver event, exert Aragorn to have an ally take part in skirmishes
					put("toby", "1_305");       // Old Toby: a [shire] possession in the support area; a draw on its own play only

					put("poison", "8_28");      // Spider Poison: a [gollum] possession in the Shadow support area; a skirmish ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ReclaimthePreciousStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Reclaim the Precious
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Assignment:</b> Assign Gollum to a companion bearing an artifact. The Free Peoples player may add a burden to prevent this and assign Gollum.
		 * 	<b>Regroup:</b> Exert Gollum twice to discard a possession. Discard this condition.
		*/

		var scn = GetScenario();

		var reclaim = scn.GetFreepsCard("reclaim");

		assertEquals("Reclaim the Precious", reclaim.getBlueprint().getTitle());
		assertNull(reclaim.getBlueprint().getSubtitle());
		assertFalse(reclaim.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, reclaim.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, reclaim.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, reclaim.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(reclaim, Keyword.SUPPORT_AREA));
		assertEquals(1, reclaim.getBlueprint().getTwilightCost());
	}

	@Test
	public void AssignsGollumToTheChosenCompanionBearingAnArtifactWhenTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var merchant = scn.GetFreepsCard("merchant");
		var follow = scn.GetFreepsCard("follow");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli, gandalf);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToSupportArea(reclaim);
		scn.MoveCardsToHand(follow);
		assertInZone(Zone.SUPPORT, reclaim);
		assertInHand(follow);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("barahir"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("retribution"));
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("glamdring"));
		scn.AttachCardsTo(merchant, scn.GetFreepsCard("narsil"));
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.MANEUVER);

		// The Merchant, an ally bearing an artifact, is made to take part in skirmishes.
		assertFalse(scn.FreepsCanAssign(merchant));
		scn.FreepsPlayCard(follow);
		assertTrue(scn.FreepsCanAssign(merchant));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.ShadowActionAvailable(reclaim));
		scn.ShadowUseCardAction(reclaim);

		// Companions bearing artifacts of two cultures; not a companion bearing a possession, not the Ring-bearer
		// with The One Ring alone, not an ally bearing an artifact.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gimli));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gandalf, frodo, merchant));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertTrue(scn.FreepsDecisionAvailable("add a burden"));
		scn.FreepsChooseNo();
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.IsCharAssignedAgainst(gimli, gollum));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayAddABurdenToPreventItAndAssignGollumHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToSupportArea(reclaim);
		assertInZone(Zone.SUPPORT, reclaim);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("barahir"));
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertEquals(0, scn.GetBurdens());
		scn.ShadowUseCardAction(reclaim);
		// Aragorn is the one companion bearing an artifact, and is chosen unasked.
		assertTrue(scn.FreepsDecisionAvailable("add a burden"));
		scn.FreepsChooseYes();

		assertEquals(1, scn.GetBurdens());
		// The Free Peoples player then assigns Gollum, to any of his companions.
		assertTrue(scn.FreepsHasCardChoiceAvailable(gandalf, frodo, aragorn));
		scn.FreepsChooseCard(gandalf);
		assertTrue(scn.IsCharAssignedAgainst(gandalf, gollum));
		assertFalse(scn.IsCharAssigned(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheWraithGollumIsAssignedAsWell() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var wraith = scn.GetShadowCard("wraith");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToSupportArea(reclaim);
		assertInZone(Zone.SUPPORT, reclaim);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("barahir"));
		scn.MoveMinionsToTable(wraith);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(reclaim));
		scn.ShadowUseCardAction(reclaim);
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(aragorn, wraith));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWithShelobAndNoGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(reclaim);
		assertInZone(Zone.SUPPORT, reclaim);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("barahir"));
		scn.MoveMinionsToTable(shelob);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(reclaim));
	}

	@Test
	public void NotOfferedWithNoCompanionBearingAnArtifact() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var gollum = scn.GetShadowCard("gollum");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(reclaim);
		assertInZone(Zone.SUPPORT, reclaim);

		scn.StartGame();
		// Gandalf bears a possession, Frodo The One Ring.
		scn.AttachCardsTo(gandalf, scn.GetFreepsCard("glamdring"));
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(reclaim));
	}

	@Test
	public void AssignmentAbilityIsNotOfferedInTheArcheryPhaseNorOnceGollumIsAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var reclaim2 = scn.GetShadowCard("reclaim2");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToSupportArea(reclaim, reclaim2);
		assertInZone(Zone.SUPPORT, reclaim, reclaim2);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, scn.GetFreepsCard("barahir"));
		scn.AttachCardsTo(gimli, scn.GetFreepsCard("retribution"));
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(reclaim));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		scn.ShadowUseCardAction(reclaim);
		scn.ShadowChooseCard(aragorn);
		scn.FreepsChooseNo();
		assertTrue(scn.IsCharAssignedAgainst(aragorn, gollum));

		// Gollum is assigned: the other copy is not offered, though Gimli bears an artifact.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowActionAvailable(reclaim2));
		assertFalse(scn.IsCharAssigned(gimli));
	}

	@Test
	public void RegroupAbilityExertsGollumTwiceToDiscardAPossessionAndThisCopy() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var reclaim2 = scn.GetShadowCard("reclaim2");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var poison = scn.GetShadowCard("poison");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barahir = scn.GetFreepsCard("barahir");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToSupportArea(reclaim, reclaim2, poison, toby);
		assertInZone(Zone.SUPPORT, reclaim, reclaim2, poison, toby);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.AttachCardsTo(gandalf, glamdring);
		assertInZone(Zone.ATTACHED, barahir, glamdring);
		// With no minion on the table the game goes straight to the regroup phase.
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum, shelob);
		// Vitality 4 with 1 wound: Gollum can exert twice.
		scn.AddWoundsToChar(gollum, 1);
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(reclaim));
		scn.ShadowUseCardAction(reclaim);
		// Gollum is the one Gollum, and exerts unasked; Shelob, a [gollum] minion of another title, does not.
		assertEquals(3, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));

		// Possessions of either side, borne or in a support area; not an artifact, not a condition.
		assertTrue(scn.ShadowHasCardChoiceAvailable(glamdring, toby, poison));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(barahir, reclaim2));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(glamdring);

		assertInDiscard(glamdring, reclaim);
		assertInZone(Zone.SUPPORT, toby, poison, reclaim2);
		assertInZone(Zone.ATTACHED, barahir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhenGollumCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(reclaim);
		assertInZone(Zone.SUPPORT, reclaim);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, glamdring);
		assertInZone(Zone.ATTACHED, glamdring);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(gollum, shelob);
		// Vitality 4 with 2 wounds: one exertion left. Shelob, unwounded, is no Gollum.
		scn.AddWoundsToChar(gollum, 2);
		assertEquals(2, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		assertFalse(scn.ShadowActionAvailable(reclaim));
		assertInZone(Zone.ATTACHED, glamdring);
	}

	@Test
	public void RegroupAbilityIsNotOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var gollum = scn.GetShadowCard("gollum");
		var gandalf = scn.GetFreepsCard("gandalf");
		var glamdring = scn.GetFreepsCard("glamdring");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToSupportArea(reclaim);
		assertInZone(Zone.SUPPORT, reclaim);

		scn.StartGame();
		scn.AttachCardsTo(gandalf, glamdring);
		assertInZone(Zone.ATTACHED, glamdring);
		scn.MoveMinionsToTable(gollum);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(gollum));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(reclaim));
	}

	@Test
	public void WithNoPossessionInPlayTheWraithGollumExertsTwiceAndOnlyThisConditionIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reclaim = scn.GetShadowCard("reclaim");
		var reclaim2 = scn.GetShadowCard("reclaim2");
		var wraith = scn.GetShadowCard("wraith");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barahir = scn.GetFreepsCard("barahir");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(reclaim, reclaim2);
		assertInZone(Zone.SUPPORT, reclaim, reclaim2);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		assertInZone(Zone.ATTACHED, barahir);
		scn.SkipToPhase(Phase.REGROUP);
		scn.MoveMinionsToTable(wraith);
		assertEquals(0, scn.GetWoundsOn(wraith));
		scn.FreepsPass();

		// An ability with a cost is offered whatever its effect will do.
		assertTrue(scn.ShadowActionAvailable(reclaim));
		scn.ShadowUseCardAction(reclaim);
		assertEquals(2, scn.GetWoundsOn(wraith));
		assertInDiscard(reclaim);
		assertInZone(Zone.SUPPORT, reclaim2);
		assertInZone(Zone.ATTACHED, barahir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
