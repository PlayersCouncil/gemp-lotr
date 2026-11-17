package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_140_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>() {{
					put("enquea", "17_140");

					// Heals the Free Peoples player makes
					put("healing1", "3_22");   // Master of Healing: [elven] Fellowship or Regroup event, cost 1: spot Elrond to heal a companion or ally
					put("healing2", "3_22");
					put("healing3", "3_22");
					put("elrond", "1_40");     // Elrond, Lord of Rivendell: ally, vitality 4; heals allies of site 3 as a turn starts
					put("peace", "7_46");      // Peace of Mind: [gandalf] Regroup event, cost 4: spot Gandalf to heal an unbound Hobbit 3 times
					put("gandalf", "1_364");   // Gandalf, The Grey Wizard: a fellowship ability only
					put("merry", "1_302");     // Merry, Friend to Sam: unbound Hobbit, vitality 4; a skirmish ability used only on purpose
					// End of the Game: [gondor] Skirmish event, cost 0: an exhausted [gondor] companion is strength +2,
					// and if he wins the skirmish he is healed or made damage +1
					put("endgame", "10_30");
					put("aragorn", "1_89");    // Aragorn, Ranger of the North: strength 8, vitality 4, resistance 6
					put("troop", "1_143");     // Troop of Uruk-hai: strength 9, vitality 4, damage +1; weapons give no strength against it
					// Faramir, Captain of Ithilien: strength 7, vitality 3.  "Maneuver: Exert Faramir to heal another
					// companion with resistance 5 or more."
					put("faramir", "18_48");
					put("harp", "18_3");       // Thorin's Harp: [dwarven] support-area condition, a tale; its ability needs a [dwarven] companion
					// Hrethel, Rider of Rohan: strength 6.  "Skirmish: Heal a minion skirmishing Hrethel to make
					// Hrethel strength +3 (limit +3)."
					put("hrethel", "6_95");
					put("savage", "1_151");    // Uruk Savage: strength 5, vitality 3; damage +1 and nothing else

					// A heal the Shadow player makes
					put("dt", "12_163");       // Dark Temptation: "Each time you play a Nazgul, you may heal the Ring-bearer twice to add a burden."
					put("rider", "12_161");    // Black Rider: [wraith] Nazgul, cost 5, site 3

					// Conditions, and cards that are none
					put("moria", "1_21");      // Lord of Moria: [dwarven] support-area condition; each Dwarf is damage +1, and there is none
					put("path", "8_42");       // A Path Appointed: [gondor] support-area condition; abilities used only on purpose
					put("doom", "8_86");       // Doom Drove Them: [rohan] support-area condition; abilities used only on purpose
					put("shake", "8_13");      // Shake Off the Shadow: [elven] support-area condition; abilities used only on purpose
					put("saga", "1_114");      // The Saga of Elendil: [gondor] condition borne by a [gondor] companion; a skirmish ability
					put("weary", "4_212");     // Weary: [isengard] Shadow condition borne by a companion; its text needs an [isengard] tracker
					put("bowmen", "1_264");    // Orc Bowmen: [sauron] Shadow support-area condition, inert without a [sauron] Orc
					put("sword", "1_299");     // Hobbit Sword: a possession, strength +2 and no text
				}},
				// Site 4 is Balin's Tomb: "Maneuver: Discard your tale from play or from hand to heal your companion."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EnqueaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Úlairë Enquëa, Duplicitous Lieutenant
		 * Unique: True
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 6
		 * Type: Minion
		 * Subtype: Nazgûl
		 * Strength: 11
		 * Vitality: 4
		 * Site Number: 3
		 * Game Text: <b>Fierce</b>.
		 * 	Each time the Free Peoples player heals a companion, you may add a burden.
		 * 	<b>Maneuver:</b> Exert Úlairë Enquëa twice to discard a condition (or two conditions if you can spot 5
		 * 	Free Peoples player's conditions).
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enquea");

		assertEquals("Úlairë Enquëa", card.getBlueprint().getTitle());
		assertEquals("Duplicitous Lieutenant", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.NAZGUL, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FIERCE));
		assertEquals(6, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(3, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachHealOfACompanionByTheFreePeoplesPlayerOffersABurdenAndAHealOfAnAllyDoesNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var healing2 = scn.GetFreepsCard("healing2");
		var healing3 = scn.GetFreepsCard("healing3");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1, healing2, healing3);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(elrond, 1);
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetBurdens());
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// A companion healed: the Shadow player may add a burden, and does.
		scn.FreepsPlayCard(healing1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, elrond));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowPass();

		// An ally healed by the same event: nothing is offered.
		scn.FreepsPlayCard(healing2);
		scn.FreepsChooseCard(elrond);
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
		scn.ShadowPass();

		// The companion healed a second time: offered again, and declined.
		scn.FreepsPlayCard(healing3);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ACompanionHealedThreeTimesByOneEventOffersABurdenThreeTimes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var merry = scn.GetFreepsCard("merry");
		var peace = scn.GetFreepsCard("peace");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(gandalf, merry);
		scn.MoveCardsToHand(peace);

		scn.StartGame();
		scn.AddWoundsToChar(merry, 3);
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(merry));

		// Merry is the one unbound Hobbit: "heal ... 3 times" is three heals, each of them answered.
		scn.FreepsPlayCard(peace);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowDeclineOptionalTrigger();

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void AHealByACompanionsAbilityAndAHealByASitesTextEachOfferABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var faramir = scn.GetFreepsCard("faramir");
		var harp = scn.GetFreepsCard("harp");
		var saga = scn.GetFreepsCard("saga");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn, faramir);
		scn.MoveCardsToSupportArea(harp);
		// The other tale of this deck is kept out of the hands the turns on the way deal: with a tale in hand the
		// site would ask which of the two to discard
		scn.MoveCardsToDiscard(saga);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.AddWoundsToChar(aragorn, 2);
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.MANEUVER);

		// Balin's Tomb
		assertEquals(4, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());

		// Faramir's own ability: Aragorn is the one wounded companion of resistance 5 or more beside him.
		scn.FreepsUseCardAction(faramir);
		assertEquals(1, scn.GetWoundsOn(faramir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		scn.ShadowPass();

		// The site's ability, which is no card of the Free Peoples player's side: he discards his tale and heals
		// his companion.
		var tomb = scn.GetCurrentSite();
		assertTrue(scn.FreepsActionAvailable(tomb));
		scn.FreepsUseCardAction(tomb);
		assertInDiscard(harp);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, faramir));
		scn.FreepsChooseCard(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void EndOfTheGameTriggersEnquea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var endgame = scn.GetFreepsCard("endgame");
		var troop = scn.GetShadowCard("troop");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(endgame);

		scn.StartGame();
		// Vitality 4 with 3 wounds: exhausted
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveMinionsToTable(troop, enquea);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);

		// 8 + 2 against 9: Aragorn wins, and the event has him healed or made damage +1.
		scn.FreepsPlayCard(endgame);
		assertEquals(10, scn.GetStrength(aragorn));
		scn.ShadowPass();
		scn.FreepsPass();

		// The event's trigger is taken ahead of the skirmish's own wound on the Troop.
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.FreepsResolveActionOrder("Required trigger from");
		scn.FreepsChooseOption("Heal");
		assertEquals(2, scn.GetWoundsOn(aragorn));

		// As a Freeps healing source, we should have the Enquea trigger
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(enquea));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(troop));
		// Enquëa, who was assigned to nobody, is fierce: the fierce assignment phase follows.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void HealingFromShadowSourcesDoesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var enquea = scn.GetShadowCard("enquea");
		var rider = scn.GetShadowCard("rider");
		var dt = scn.GetShadowCard("dt");
		scn.MoveCardsToHand(rider, dt);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 3);
		scn.MoveMinionsToTable(enquea);
		scn.SetTwilight(10);
		scn.FreepsPass();

		scn.ShadowPlayCard(dt);
		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetWoundsOn(frodo));

		// Dark Temptation answers the Nazgul played: the Shadow player heals the Ring-bearer twice to add a burden.
		scn.ShadowPlayCard(rider);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(dt));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(1, scn.GetWoundsOn(frodo));

		// We should see +1 burden from Dark Temptation, but no burdens added from Enquea as DT is a shadow card.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AMinionHealedByTheFreePeoplesPlayerOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hrethel = scn.GetFreepsCard("hrethel");
		var savage = scn.GetShadowCard("savage");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(hrethel);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, enquea);
		scn.AddWoundsToChar(savage, 1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hrethel, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(hrethel);

		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(6, scn.GetStrength(hrethel));

		scn.FreepsUseCardAction(hrethel);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(6 + 3, scn.GetStrength(hrethel));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileNoBurdenCanBeAddedToAHinderedRingBearer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var healing1 = scn.GetFreepsCard("healing1");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing1);

		scn.StartGame();
		scn.AddWoundsToChar(aragorn, 1);
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.REGROUP);
		scn.HinderCard(frodo);

		// Aragorn is the one wounded character.  The heal is made; the offer costs nothing and would add nothing.
		scn.FreepsPlayCard(healing1);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ManeuverActionExertsEnqueaTwiceToDiscardOneConditionWhileFourFreePeoplesConditionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var moria = scn.GetFreepsCard("moria");
		var path = scn.GetFreepsCard("path");
		var doom = scn.GetFreepsCard("doom");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var enquea = scn.GetShadowCard("enquea");
		var weary = scn.GetShadowCard("weary");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);
		// Four Free Peoples conditions, three in the support area and one on a companion
		scn.MoveCardsToSupportArea(moria, path, doom);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(frodo, sword);
		// Two Shadow conditions: with them six conditions can be spotted, and four of the Free Peoples player's
		scn.MoveCardsToSupportArea(bowmen);
		scn.AttachCardsTo(aragorn, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.ShadowActionAvailable(enquea));
		scn.ShadowUseCardAction(enquea);
		assertEquals(2, scn.GetWoundsOn(enquea));

		// Exactly one condition, of either side, in a support area or on a character; a possession and an ally are none.
		assertTrue(scn.ShadowHasCardChoiceAvailable(moria, path, doom, saga, bowmen, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, elrond));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(saga);

		assertInDiscard(saga);
		assertInZone(Zone.SUPPORT, moria);
		assertInZone(Zone.SUPPORT, path);
		assertInZone(Zone.SUPPORT, doom);
		assertInZone(Zone.SUPPORT, bowmen);
		assertAttachedTo(weary, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionDiscardsTwoConditionsWhileFiveFreePeoplesConditionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var moria = scn.GetFreepsCard("moria");
		var path = scn.GetFreepsCard("path");
		var doom = scn.GetFreepsCard("doom");
		var shake = scn.GetFreepsCard("shake");
		var saga = scn.GetFreepsCard("saga");
		var sword = scn.GetFreepsCard("sword");
		var enquea = scn.GetShadowCard("enquea");
		var weary = scn.GetShadowCard("weary");
		var bowmen = scn.GetShadowCard("bowmen");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);
		// Five Free Peoples conditions, four in the support area and one on a companion
		scn.MoveCardsToSupportArea(moria, path, doom, shake);
		scn.AttachCardsTo(aragorn, saga);
		scn.AttachCardsTo(frodo, sword);
		scn.MoveCardsToSupportArea(bowmen);
		scn.AttachCardsTo(aragorn, weary);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertEquals(0, scn.GetWoundsOn(enquea));
		assertTrue(scn.ShadowActionAvailable(enquea));
		scn.ShadowUseCardAction(enquea);
		assertEquals(2, scn.GetWoundsOn(enquea));

		// Exactly two, of any of the conditions, a Shadow one among them.
		assertTrue(scn.ShadowHasCardChoiceAvailable(moria, path, doom, shake, saga, bowmen, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, elrond));
		assertEquals(2, scn.ShadowGetChoiceMin());
		assertEquals(2, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(path, bowmen);

		assertInDiscard(path);
		assertInDiscard(bowmen);
		assertInZone(Zone.SUPPORT, moria);
		assertInZone(Zone.SUPPORT, doom);
		assertInZone(Zone.SUPPORT, shake);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(weary, aragorn);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverActionIsNotOfferedOnceEnqueaCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var moria = scn.GetFreepsCard("moria");
		var enquea = scn.GetShadowCard("enquea");
		scn.MoveCardsToSupportArea(moria);

		scn.StartGame();
		scn.MoveMinionsToTable(enquea);
		scn.AddWoundsToChar(enquea, 1);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 4 with 1 wound: two exertions can be paid.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowActionAvailable(enquea));

		// With 2 wounds a second exertion would be her last point of vitality.
		scn.AddWoundsToChar(enquea, 1);
		scn.ShadowRefreshDecision();
		assertEquals(2, scn.GetVitality(enquea));
		assertInZone(Zone.SUPPORT, moria);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowActionAvailable(enquea));
	}
}
