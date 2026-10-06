package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("shelob", "103_13");
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum], strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: a [wraith] Gollum, strength 5, vitality 4; an assignment ability never used here
					put("runner", "1_178");         // Goblin Runner: strength 5, vitality 1
					put("savage", "1_151");         // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else

					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability never used here
					put("glorfindel", "9_16");      // Glorfindel, Revealed in Wrath: strength 9, vitality 3; a skirmish ability never used here
					put("guard", "1_7");            // Dwarf Guard: strength 4, vitality 2, no text in play
					put("elrond", "1_40");          // Elrond, Lord of Rivendell: ally, home site 3, strength 8, vitality 4
					put("gauntlets", "4_112");      // Boromir's Gauntlets: Skirmish: exert bearer to cancel a skirmish involving bearer; the Shadow player may remove (1) to prevent this
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ShelobStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Shelob, Terror of Cirith Ungol
		 * Unique: true
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Spider
		 * Strength: 8
		 * Vitality: 8
		 * Site Number: 8
		 * Game Text: Fierce.  Enduring.
		 * 	Each time a skirmish involving Shelob ends, you may hinder each character she was skirmishing.
		 * 	Skirmish: If Gollum is skirmishing, exert Gollum and Shelob to hinder a character he is skirmishing.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("shelob");

		assertEquals("Shelob", card.getBlueprint().getTitle());
		assertEquals("Terror of Cirith Ungol", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.SPIDER, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(8, card.getBlueprint().getStrength());
		assertEquals(8, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void PlayedFromHandForSixAndTwoMoreWhileRoaming() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCardsToHand(shelob);

		scn.StartGame();
		scn.SetTwilight(9);
		scn.FreepsPass();

		// 9 set + 1 for Frodo + 2 for site 2
		assertEquals(12, scn.GetTwilight());
		assertInZone(Zone.HAND, shelob);
		assertTrue(scn.ShadowPlayAvailable(shelob));

		scn.ShadowPlayCard(shelob);
		// 6 to play + 2 roaming: her site number is 8 and the fellowship is at site 2
		assertEquals(4, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, shelob);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachSkirmishShelobFightsMayHinderTheCharacterSheFoughtAndNobodyElse() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveCompanionsToTable(aragorn, glorfindel);
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		// Site 3 is Elrond's home, where an ally may be assigned to a skirmish.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(shelob, savage, runner);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, shelob },
				new PhysicalCardImpl[] { glorfindel, runner }
		);
		scn.ShadowAssignToMinions(aragorn, savage);

		// A skirmish Shelob is not in: Glorfindel (9) kills the Runner (5), and nothing is offered.
		scn.FreepsResolveSkirmish(glorfindel);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(glorfindel));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));

		// Shelob's skirmish: Aragorn (8) loses to Shelob and the Savage (8 + 5).
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, shelob));
		scn.PassSkirmishActions();

		// The offer comes when the skirmish is over: Aragorn has already taken its wounds,
		// 1 for losing + 1 for the Savage's damage +1.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertFalse(scn.IsHindered(aragorn));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(aragorn));
		// Not the companion whose skirmish was against another minion, not the minion who fought beside her,
		// not Shelob, and nobody who stood by.
		assertFalse(scn.IsHindered(glorfindel));
		assertFalse(scn.IsHindered(savage));
		assertFalse(scn.IsHindered(shelob));
		assertFalse(scn.IsHindered(elrond));
		assertFalse(scn.IsHindered(scn.GetRingBearer()));

		// Shelob is fierce: a second skirmish in the same turn, this time against an ally.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(elrond, shelob);
		scn.FreepsResolveSkirmish(elrond);
		assertTrue(scn.IsCharSkirmishingAgainst(elrond, shelob));
		assertEquals(0, scn.GetWoundsOn(elrond));
		// 8 against 8: the Shadow side wins a tie.
		scn.PassFierceSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertFalse(scn.IsHindered(elrond));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(elrond));
		assertFalse(scn.IsHindered(glorfindel));
		assertFalse(scn.IsHindered(shelob));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishShelobLosesHasEndedAllTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var runner = scn.GetShadowCard("runner");
		var glorfindel = scn.GetFreepsCard("glorfindel");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(glorfindel, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, runner);
		scn.SkipToAssignments();

		// Glorfindel (9) beats Shelob (8).  Aragorn is assigned to a Goblin Runner, and his skirmish waits.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { glorfindel, shelob },
				new PhysicalCardImpl[] { aragorn, runner }
		);
		scn.FreepsResolveSkirmish(glorfindel);
		assertEquals(0, scn.GetWoundsOn(shelob));
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertEquals(0, scn.GetWoundsOn(glorfindel));
		assertFalse(scn.IsHindered(glorfindel));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(glorfindel));
		assertFalse(scn.IsHindered(shelob));
		// A companion assigned to another minion, his skirmish not yet begun, is not one she was skirmishing.
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.IsCharAssignedAgainst(aragorn, runner));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void ASkirmishThatIsCancelledHasEndedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gauntlets = scn.GetFreepsCard("gauntlets");

		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, gauntlets);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn exerts to cancel his skirmish, and the Shadow player does not pay (1) to stop it.
		assertTrue(scn.FreepsActionAvailable(gauntlets));
		scn.FreepsUseCardAction(gauntlets);
		scn.ShadowChooseNo();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		assertFalse(scn.IsHindered(aragorn));

		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.IsHindered(aragorn));
		// Nobody won or lost: Aragorn has only the wound of his exertion.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void TheOfferMayBeDeclinedAndNobodyIsHindered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(shelob));
		scn.ShadowDeclineOptionalTrigger();

		assertFalse(scn.IsHindered(aragorn));
		assertFalse(scn.IsHindered(shelob));
		// 8 against 8: Aragorn lost the tie and has its wound, and may be assigned to her again.
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertTrue(scn.FreepsCanAssign(aragorn));
	}

	@Test
	public void NothingIsOfferedWhenTheCompanionSheFoughtDidNotSurvive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(guard, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob);
		scn.SkipToAssignments();

		// 8 against 4: the Guard is overwhelmed.
		scn.FreepsAssignToMinions(guard, shelob);
		scn.FreepsResolveSkirmish(guard);
		assertInZone(Zone.FREE_CHARACTERS, guard);
		scn.PassSkirmishActions();

		// With nobody left to hinder, the Shadow player is not asked.
		assertInZone(Zone.DEAD, guard);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WhileGollumSkirmishesShelobExertsWithHimToHinderTheCharacterHeFights() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gollum },
				new PhysicalCardImpl[] { glorfindel, shelob }
		);

		// Gollum's skirmish; Shelob is assigned to Glorfindel and her own has not begun.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertFalse(scn.IsHindered(aragorn));
		assertTrue(scn.ShadowActionAvailable(shelob));

		scn.ShadowUseCardAction(shelob);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.IsHindered(aragorn));
		// The companion waiting for Shelob is not a character Gollum is skirmishing.
		assertFalse(scn.IsHindered(glorfindel));
		assertTrue(scn.IsCharAssignedAgainst(glorfindel, shelob));
		assertFalse(scn.IsHindered(gollum));
		assertFalse(scn.IsHindered(shelob));

		// A hindered character leaves his skirmish, which ends there: Shelob's is next.
		assertFalse(scn.IsCharSkirmishing(gollum));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
	}

	@Test
	public void NotOfferedInAnotherSkirmishWhileGollumIsOnlyAssignedAndOfferedOnceHisOwnBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");
		var glorfindel = scn.GetFreepsCard("glorfindel");

		scn.MoveCompanionsToTable(aragorn, glorfindel);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, shelob },
				new PhysicalCardImpl[] { glorfindel, gollum }
		);

		// Shelob's own skirmish first.  Gollum is assigned to Glorfindel and is not yet skirmishing.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(shelob));
		assertTrue(scn.IsCharAssignedAgainst(glorfindel, gollum));
		assertFalse(scn.IsCharSkirmishing(gollum));
		assertFalse(scn.ShadowActionAvailable(shelob));

		scn.ShadowPass();
		scn.ShadowDeclineOptionalTrigger();

		// Gollum's skirmish: the same two minions, neither of them touched since.
		scn.FreepsResolveSkirmish(glorfindel);
		scn.FreepsPass();
		assertTrue(scn.IsCharSkirmishing(gollum));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.ShadowActionAvailable(shelob));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AGollumOfAnotherCultureIsGollumAndShelobNeedNotBeAssigned() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var wraithgollum = scn.GetShadowCard("wraithgollum");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, wraithgollum);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, wraithgollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertFalse(scn.IsCharAssigned(shelob));
		assertEquals(0, scn.GetWoundsOn(wraithgollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertTrue(scn.ShadowActionAvailable(shelob));

		scn.ShadowUseCardAction(shelob);

		assertEquals(1, scn.GetWoundsOn(wraithgollum));
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertTrue(scn.IsHindered(aragorn));
		// The skirmish was not Shelob's, so its end brings her no offer; her fierce assignment is next.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void AnAllyWhomGollumIsSkirmishingIsHinderedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var elrond = scn.GetFreepsCard("elrond");

		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		// Site 3 is Elrond's home, where an ally may be assigned to a skirmish.
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(shelob, gollum);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());

		scn.FreepsAssignToMinions(elrond, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(elrond, gollum));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertFalse(scn.IsHindered(elrond));
		assertTrue(scn.ShadowActionAvailable(shelob));

		scn.ShadowUseCardAction(shelob);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertTrue(scn.IsHindered(elrond));
		assertFalse(scn.IsHindered(scn.GetRingBearer()));
		assertFalse(scn.IsCharSkirmishing(gollum));
		// On to Shelob's fierce assignment.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void WithShelobInGollumsSkirmishTheCompanionIsHinderedOnceAndItsEndBringsNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowAssignToMinions(aragorn, shelob);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, shelob));
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, gollum));
		assertTrue(scn.ShadowActionAvailable(shelob));

		scn.ShadowUseCardAction(shelob);

		assertEquals(1, scn.GetWoundsOn(gollum));
		assertEquals(1, scn.GetWoundsOn(shelob));
		assertTrue(scn.IsHindered(aragorn));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// The skirmish was Shelob's too and has ended, but the only character she was skirmishing is hindered
		// already: there is nothing for her own offer to do, and it is not made.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void NotOfferedWhileGollumCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(gollum, 3);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishing(gollum));
		assertTrue(scn.IsExhausted(gollum));
		assertEquals(0, scn.GetWoundsOn(shelob));
		assertFalse(scn.ShadowActionAvailable(shelob));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedWhileShelobCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var shelob = scn.GetShadowCard("shelob");
		var gollum = scn.GetShadowCard("gollum");
		var aragorn = scn.GetFreepsCard("aragorn");

		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(shelob, gollum);
		// Vitality 8 with 7 wounds: exhausted.
		scn.AddWoundsToChar(shelob, 7);
		scn.SkipToAssignments();

		scn.FreepsAssignToMinions(aragorn, gollum);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.IsCharSkirmishing(gollum));
		assertTrue(scn.IsExhausted(shelob));
		assertEquals(0, scn.GetWoundsOn(gollum));
		assertFalse(scn.ShadowActionAvailable(shelob));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
