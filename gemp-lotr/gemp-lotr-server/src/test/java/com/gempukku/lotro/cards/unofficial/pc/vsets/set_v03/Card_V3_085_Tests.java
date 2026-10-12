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

public class Card_V3_085_Tests
{
	// Site 2 of this path is Ettenmoors: "Skirmish: Exert your companion or minion to make that character
	// strength +2." The tests of the trigger use it to exert whichever companion they need.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "103_85");
					put("eowyn", "5_122");      // Éowyn, Daughter of Éomund: [rohan] Man, strength 6, vitality 3; a maneuver ability never used here
					put("rider1", "4_286");     // Rider of Rohan: [rohan] Man companion, strength 5, vitality 3; his ability needs a mount
					put("rider2", "4_286");
					put("scout", "5_90");       // Rohirrim Scout: valiant [rohan] Man companion, vitality 3; a regroup ability never used here
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8
					put("hlafwine", "4_281");   // Hlafwine: [rohan] Man ally. Skirmish: exert Hlafwine to make a [rohan] Man strength +2
					put("guma", "4_277");       // Guma: [rohan] Man ally, vitality 2. Fellowship: exert Guma to heal a [rohan] Man
					put("leowyn", "7_239");     // Léowyn: [rohan] Man ally whose name is not Éowyn's; her ability needs initiative
					put("shield", "8_88");      // Éowyn's Shield: a [rohan] possession that is not Éowyn either; bearer must be a [rohan] Man
					put("mount1", "4_287");     // Rider's Mount: bearer must be a [rohan] Man
					put("mount2", "4_287");
					put("iamnoman", "103_84");  // I Am No Man: exert a skirmishing valiant companion twice to weaken a minion

					put("soldier1", "1_271");   // Orc Soldier: strength 7, vitality 2. Skirmish: exert this minion to wound a character he is skirmishing
					put("soldier2", "1_271");
					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("troop", "1_177");      // Goblin Patrol Troop: strength 13, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The [rohan] allies are at home at site 4 of the Towers path, where they can skirmish.
	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("merry", "103_85");
					put("hlafwine", "4_281");   // Hlafwine: [rohan] Man ally, strength 4, vitality 2, home site 4 of the Towers path
					put("soldier", "1_271");    // Orc Soldier: strength 7, vitality 2. Skirmish: exert this minion to wound a character he is skirmishing
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MerryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: Merry, Master Holbytla
		 * Unique: true
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 6
		 * Signet: Aragorn
		 * Game Text: Valiant. Enduring.
		 * 	While you can spot 3 [rohan] companions (or Eowyn), Merry is considered a [Rohan] Man.
		 * 	Each time another [rohan] Man exerts, you may exert Merry to heal that Man.
		 * 	Skirmish: If he is not assigned to a skirmish, hinder Merry to immediately resolve a skirmish involving a [rohan] Man.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("merry");

		assertEquals("Merry", card.getBlueprint().getTitle());
		assertEquals("Master Holbytla", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(card, Keyword.ENDURING));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertEquals(Signet.ARAGORN, card.getBlueprint().getSignet());
	}

	@Test
	public void PlayedAndIsHimselfOneOfTheThreeRohanCompanionsThatMakeHimAMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		scn.MoveCompanionsToTable(rider1, rider2);
		scn.MoveCardsToHand(merry);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(merry));

		scn.FreepsPlayCard(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		// The two Riders and Merry himself are the three [rohan] companions.
		assertTrue(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void BecomesAManWhenAThirdRohanCompanionIsPlayedAndNotForAnAllyOrACompanionOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var leowyn = scn.GetFreepsCard("leowyn");
		var shield = scn.GetFreepsCard("shield");
		var mount1 = scn.GetFreepsCard("mount1");
		var mount2 = scn.GetFreepsCard("mount2");
		// Two [rohan] companions, and beside them a [rohan] ally, a companion of another culture, and two cards
		// with Éowyn's name in theirs that are not her.
		scn.MoveCompanionsToTable(merry, rider1, aragorn);
		scn.MoveAlliesToTable(leowyn);
		scn.AttachCardsTo(rider1, shield);
		scn.MoveCardsToHand(rider2, mount1, mount2);

		scn.StartGame();

		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.IsRace(merry, Race.HOBBIT));

		// Rider's Mount must be borne by a [rohan] Man.
		scn.FreepsPlayCard(mount1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider1, leowyn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, aragorn));
		scn.FreepsChooseCard(leowyn);
		assertAttachedTo(mount1, leowyn);

		scn.FreepsPlayCard(rider2);

		assertTrue(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.IsRace(merry, Race.HOBBIT));

		scn.FreepsPlayCard(mount2);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rider1, rider2, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(merry);
		assertAttachedTo(mount2, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EowynAloneMakesHimAManAndHeIsNoLongerOneWhenSheDies() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(eowyn);

		scn.StartGame();

		assertFalse(scn.IsRace(merry, Race.MAN));

		scn.FreepsPlayCard(eowyn);

		// Merry and Éowyn are only two [rohan] companions: it is her name that does it.
		assertTrue(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.IsRace(merry, Race.HOBBIT));

		// Vitality 3: three wounds kill her as the phase ends.
		scn.AddWoundsToChar(eowyn, 3);
		scn.FreepsPass();

		assertInZone(Zone.DEAD, eowyn);
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertTrue(scn.IsRace(merry, Race.HOBBIT));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachTimeAnotherRohanManExertsMerryMayExertToHealThatMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider1 = scn.GetFreepsCard("rider1");
		var rider2 = scn.GetFreepsCard("rider2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		scn.MoveCompanionsToTable(merry, rider1, rider2, aragorn);
		scn.MoveAlliesToTable(hlafwine);
		// A second wounded [rohan] Man, who is not the one that exerts.
		scn.AddWoundsToChar(rider2, 1);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, troop);
		var ettenmoors = scn.GetCurrentSite();

		// A [rohan] Man companion exerts, and the offer is declined.
		assertEquals(0, scn.GetWoundsOn(rider1));
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(rider1);
		assertEquals(1, scn.GetWoundsOn(rider1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(rider1));
		assertEquals(1, scn.GetWoundsOn(rider2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// The same Man exerts again and the offer is made again.
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(rider1);
		assertEquals(2, scn.GetWoundsOn(rider1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsChooseOptionalTrigger(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(rider1));
		assertEquals(1, scn.GetWoundsOn(rider2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// A [rohan] Man who is an ally exerts for his own ability.
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		scn.FreepsUseCardAction(hlafwine);
		assertEquals(1, scn.GetWoundsOn(hlafwine));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsChooseOptionalTrigger(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(hlafwine));
		assertEquals(1, scn.GetWoundsOn(rider2));
		// Hlafwine's own strength +2 goes to a [rohan] Man.
		scn.FreepsChooseCard(rider1);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedForAGondorManNorAWoundNorMerrysOwnExertion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, eowyn, aragorn);

		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(eowyn, soldier);
		var ettenmoors = scn.GetCurrentSite();

		// With Éowyn in play Merry is himself a [rohan] Man.
		assertTrue(scn.IsRace(merry, Race.MAN));

		// A [gondor] Man exerts.
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A minion exerts, and a [rohan] Man takes a wound that is no exertion.
		scn.ShadowUseCardAction(soldier);
		assertEquals(1, scn.GetWoundsOn(soldier));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Merry, a [rohan] Man himself just now, exerts.
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(merry);
		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();

		// On the same board another [rohan] Man's exertion does bring the offer; Merry's exertion to pay for
		// it brings no second one.
		scn.FreepsUseCardAction(ettenmoors);
		scn.FreepsChooseCard(eowyn);
		assertEquals(2, scn.GetWoundsOn(eowyn));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsChooseOptionalTrigger(merry);
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotOfferedOnceMerryIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var guma = scn.GetFreepsCard("guma");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(guma);
		// Vitality 4: one exertion left.
		scn.AddWoundsToChar(merry, 2);

		scn.StartGame();

		scn.FreepsUseCardAction(guma);
		assertEquals(1, scn.GetWoundsOn(guma));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsChooseOptionalTrigger(merry);
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Guma exerts again. Merry cannot exert, so nothing is offered; Guma's own ability then heals the only
		// wounded [rohan] Man, himself.
		scn.FreepsUseCardAction(guma);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(guma));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AManWhoExertsTwiceToPayOneCostIsOfferedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var scout = scn.GetFreepsCard("scout");
		var iamnoman = scn.GetFreepsCard("iamnoman");
		scn.MoveCompanionsToTable(merry, scout);
		scn.MoveCardsToHand(iamnoman);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(scout, troop);

		assertEquals(0, scn.GetWoundsOn(scout));
		assertEquals(0, scn.GetWoundsOn(merry));

		// I Am No Man exerts the skirmishing valiant companion twice.
		scn.FreepsPlayCard(iamnoman);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsChooseOptionalTrigger(merry);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(merry));
		scn.FreepsChooseOptionalTrigger(merry);

		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(scout));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void HinderingMerryResolvesARohanMansSkirmishWithNoMoreActionsForEitherPlayer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider = scn.GetFreepsCard("rider1");
		var eowyn = scn.GetFreepsCard("eowyn");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		scn.MoveCompanionsToTable(merry, rider, eowyn);
		scn.MoveAlliesToTable(hlafwine);

		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier1, soldier2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{rider, soldier1}, new PhysicalCardImpl[]{eowyn, soldier2});

		// An ordinary skirmish first: when the Free Peoples player passes, the Orc Soldier may use his ability.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.FreepsActionAvailable(merry));
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(soldier1));
		scn.ShadowPass();
		// Strength 5 against 7.
		assertEquals(1, scn.GetWoundsOn(rider));

		scn.FreepsResolveSkirmish(eowyn);
		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.FreepsActionAvailable(merry));
		assertTrue(scn.FreepsActionAvailable(hlafwine));

		scn.FreepsUseCardAction(merry);

		assertTrue(scn.IsHindered(merry));
		// The game still asks each player in turn, but there is nothing either can do: not the Orc Soldier's
		// ability, not the site's, not Hlafwine's.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(soldier2));
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hlafwine));
		assertFalse(scn.FreepsActionAvailable(scn.GetCurrentSite()));
		scn.FreepsPass();

		// Resolved on the strengths as they stood, 6 against 7: Éowyn loses and takes the wound.
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(0, scn.GetWoundsOn(soldier2));
		assertFalse(scn.IsCharSkirmishing(eowyn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsOfferedOnlyWhileMerryIsNotAssignedAndAgainOnceHisOwnSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var rider = scn.GetFreepsCard("rider1");
		var eowyn = scn.GetFreepsCard("eowyn");
		scn.MoveCompanionsToTable(merry, rider, eowyn);

		var soldier = scn.GetShadowCard("soldier1");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier, runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{rider, soldier},
				new PhysicalCardImpl[]{merry, runner},
				new PhysicalCardImpl[]{eowyn, savage});

		// A [rohan] Man's skirmish, with Merry assigned to one that has not begun.
		scn.FreepsResolveSkirmish(rider);
		assertTrue(scn.IsCharSkirmishing(rider));
		assertTrue(scn.IsCharAssigned(merry));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();

		// His own skirmish. Strength 3 against the Goblin Runner's 5: he loses and takes a wound.
		scn.FreepsResolveSkirmish(merry);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(merry));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(merry));

		// Éowyn's skirmish: Merry's is over, so he is no longer assigned.
		scn.FreepsResolveSkirmish(eowyn);
		assertFalse(scn.IsCharAssigned(merry));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);

		assertTrue(scn.IsHindered(merry));
		assertFalse(scn.ShadowActionAvailable(scn.GetCurrentSite()));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPass();
		scn.FreepsPass();
		// Strength 6 against the Uruk Savage's 5: this time the Man wins.
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARohanManAllyFightingAtHisHomeSiteIsARohanManInASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var merry = scn.GetFreepsCard("merry");
		var hlafwine = scn.GetFreepsCard("hlafwine");
		scn.MoveCompanionsToTable(merry);
		scn.MoveAlliesToTable(hlafwine);

		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(soldier);
		// The fellowship moves to site 4, Hlafwine's home.
		scn.SkipToAssignments();
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsAssignAndResolve(hlafwine, soldier);

		assertTrue(scn.IsCharSkirmishing(hlafwine));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);

		assertTrue(scn.IsHindered(merry));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(soldier));
		scn.ShadowPass();
		scn.FreepsPass();
		// Strength 4 against 7.
		assertEquals(1, scn.GetWoundsOn(hlafwine));
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InASkirmishOfAGondorManHinderingMerryResolvesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);

		var soldier = scn.GetShadowCard("soldier1");

		scn.StartGame();
		scn.MoveMinionsToTable(soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(aragorn, soldier);

		// Hindering Merry is all the ability costs, so it is on offer whatever the skirmish.
		assertFalse(scn.IsHindered(merry));
		assertTrue(scn.FreepsActionAvailable(merry));

		scn.FreepsUseCardAction(merry);

		assertTrue(scn.IsHindered(merry));
		// No [rohan] Man is fighting, so the skirmish goes on: the Orc Soldier uses his ability and the Free
		// Peoples player is asked again.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowActionAvailable(soldier));
		scn.ShadowUseCardAction(soldier);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(scn.GetCurrentSite()));
	}
}
