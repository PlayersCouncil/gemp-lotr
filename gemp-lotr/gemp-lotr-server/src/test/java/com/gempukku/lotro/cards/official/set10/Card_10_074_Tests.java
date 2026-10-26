package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_074_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unyielding", "10_74");
					put("guard1", "5_83");      // Household Guard: [rohan] Man, strength 6, vitality 3; "Valiant." is its whole text
					put("guard2", "5_83");
					put("theoden", "4_292");    // Théoden, Son of Thengel: [rohan] Man, strength 6; a regroup ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4
					put("merry", "103_85");     // Merry, Master Holbytla: a [rohan] Hobbit, a Man only beside 3 [rohan] companions or Éowyn
					// [rohan] possessions: a mount a [rohan] Man bears (strength +1; its play trigger is never set off
					// here), and one that nobody bears (support area; a maneuver ability used only on purpose).
					put("firefoot", "13_126");  // Firefoot, Mearas of The Mark
					put("signalfire", "103_86"); // Northern Signal-fire, Flame of Calenhad
					// Items that are no [rohan] possession: a [rohan] artifact, and a possession of another culture.
					put("herugrim", "54_280");  // Herugrim: [rohan] artifact, bearer must be Théoden
					put("sword", "1_112");      // Ranger's Sword: [gondor] possession, bearer must be Aragorn

					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3; "Damage +1." is its whole text
					put("savage2", "1_151");
					put("chieftain", "1_266");  // Orc Chieftain: strength 7, vitality 2; answers only a weapon played on him
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; its text is about being played
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetTowersScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("unyielding", "10_74");
					put("herdsman", "7_247");   // Rohirrim Herdsman: [rohan] Man ally, home 3T, strength 4, vitality 2; a regroup ability used only on purpose
					put("signalfire", "103_86"); // Northern Signal-fire, Flame of Calenhad: a [rohan] possession in the support area

					put("whelp", "7_202");      // Morgul Whelp: strength 3, vitality 1 while the Ring-bearer has no wound
					put("chieftain", "1_266");  // Orc Chieftain: strength 7, vitality 2; answers only a weapon played on him
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UnyieldingStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Unyielding
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If your [rohan] Man wins a skirmish, discard a [rohan] possession to wound a minion or to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("unyielding");

		assertEquals("Unyielding", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WhenYourRohanManWinsDiscardARohanPossessionToWoundAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unyielding = scn.GetFreepsCard("unyielding");
		var guard = scn.GetFreepsCard("guard1");
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var firefoot = scn.GetFreepsCard("firefoot");
		var signalfire = scn.GetFreepsCard("signalfire");
		var herugrim = scn.GetFreepsCard("herugrim");
		var sword = scn.GetFreepsCard("sword");
		var savage = scn.GetShadowCard("savage1");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveCompanionsToTable(guard, theoden, aragorn);
		scn.AttachCardsTo(guard, firefoot);
		scn.AttachCardsTo(theoden, herugrim);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToSupportArea(signalfire);
		scn.MoveCardsToHand(unyielding);
		assertInHand(unyielding);
		assertAttachedTo(herugrim, theoden);
		assertAttachedTo(sword, aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, chieftain);
		// Vitality 2 with 1 wound: the Chieftain, in no skirmish, is exhausted.
		scn.AddWoundsToChar(chieftain, 1);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInZone(Zone.SUPPORT, signalfire);
		assertAttachedTo(firefoot, guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(guard);
		// 6 + 1 for Firefoot against 5.
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unyielding));

		scn.FreepsChooseOptionalTrigger(unyielding);

		// The cost: a [rohan] possession, borne or in the support area; not a [rohan] artifact, nor a
		// possession of another culture.
		assertTrue(scn.FreepsHasCardChoiceAvailable(firefoot, signalfire));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(herugrim, sword));
		scn.FreepsChooseCard(signalfire);
		assertInDiscard(signalfire);
		assertAttachedTo(firefoot, guard);
		assertAttachedTo(herugrim, theoden);
		assertAttachedTo(sword, aragorn);

		// No site is controlled, so nothing can be liberated and the wound is not a choice between the two.
		// Any minion: the one just beaten, or an exhausted one in no skirmish; not a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, chieftain));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, theoden, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(chieftain);

		// Wounded, and so killed.
		assertInDiscard(chieftain);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(unyielding);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MayLiberateTheHighestSiteAnOpponentControlsInsteadOfWoundingAMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unyielding = scn.GetFreepsCard("unyielding");
		var guard = scn.GetFreepsCard("guard1");
		var firefoot = scn.GetFreepsCard("firefoot");
		var savage = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, firefoot);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.ShadowTakeControlOfSite(site2);
		scn.MoveCardsToHand(unyielding);
		assertInHand(unyielding);
		assertAttachedTo(firefoot, guard);
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.ShadowControls(site2));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unyielding));

		scn.FreepsChooseOptionalTrigger(unyielding);
		// Firefoot is the only [rohan] possession, and is discarded.
		assertInDiscard(firefoot);

		// A site to liberate and a minion to wound: the player chooses.
		assertTrue(scn.FreepsChoiceAvailable("Liberate a site"));
		assertTrue(scn.FreepsChoiceAvailable("Wound a minion"));
		scn.FreepsChoose("Liberate a site");

		// The site with the highest number that an opponent controls, by rule: site 2, and not site 1.
		assertFalse(scn.IsSiteControlled(site2));
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertInDiscard(unyielding);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoMinionLeftToWoundTheSiteIsLiberatedWithoutAChoice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unyielding = scn.GetFreepsCard("unyielding");
		var guard = scn.GetFreepsCard("guard1");
		var firefoot = scn.GetFreepsCard("firefoot");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, firefoot);

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(site1);
		scn.MoveCardsToHand(unyielding);
		assertInHand(unyielding);
		assertAttachedTo(firefoot, guard);
		scn.FreepsPass();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, runner);
		scn.FreepsResolveSkirmish(guard);
		// 7 against 5: the Runner, vitality 1, is killed.
		scn.FreepsPass();
		scn.ShadowPass();

		assertInDiscard(runner);
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unyielding));

		scn.FreepsChooseOptionalTrigger(unyielding);

		assertInDiscard(firefoot);
		assertFalse(scn.IsSiteControlled(site1));
		assertInDiscard(unyielding);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARohanManAllyWhoWinsASkirmishAtHisHomeSiteMayUseItTheSame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetTowersScenario();

		var unyielding = scn.GetFreepsCard("unyielding");
		var herdsman = scn.GetFreepsCard("herdsman");
		var signalfire = scn.GetFreepsCard("signalfire");
		var whelp = scn.GetShadowCard("whelp");
		var chieftain = scn.GetShadowCard("chieftain");
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToSupportArea(signalfire);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(unyielding);
		assertInHand(unyielding);
		assertInZone(Zone.SUPPORT, signalfire);
		// The fellowship moves to site 3, the Herdsman's home, where he may be assigned to a skirmish.
		scn.FreepsPass();
		scn.MoveMinionsToTable(whelp, chieftain);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(herdsman, whelp);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(herdsman);
		// 4 against 3: the Whelp, vitality 1, is killed.
		scn.FreepsPass();
		scn.ShadowPass();

		assertEquals(3, scn.GetCurrentSiteNumber());
		assertInDiscard(whelp);
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unyielding));

		scn.FreepsChooseOptionalTrigger(unyielding);

		// The Signal-fire is the only [rohan] possession and the Chieftain the only minion: both without a choice.
		assertInDiscard(signalfire);
		assertEquals(1, scn.GetWoundsOn(chieftain));
		assertInDiscard(unyielding);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWhenAManOfAnotherCultureOrARohanHobbitWinsOrTheRohanManLoses() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unyielding = scn.GetFreepsCard("unyielding");
		var guard = scn.GetFreepsCard("guard1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var firefoot = scn.GetFreepsCard("firefoot");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var chieftain = scn.GetShadowCard("chieftain");
		// Two [rohan] companions, Merry and the Guard: Merry is no Man.
		scn.MoveCompanionsToTable(guard, aragorn, merry);
		scn.AttachCardsTo(guard, firefoot);
		scn.MoveCardsToHand(unyielding);
		assertInHand(unyielding);

		scn.StartGame();
		assertAttachedTo(firefoot, guard);
		scn.MoveMinionsToTable(savage1, savage2, chieftain);
		// Merry at 6, to beat a minion of 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, merry, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, savage1}, new PhysicalCardImpl[]{merry, savage2},
				new PhysicalCardImpl[]{guard, chieftain});

		// Aragorn's 8 against 5: a [gondor] Man wins, with a [rohan] possession there to pay, and no offer.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// Merry's 6 against 5: a [rohan] companion who is no Man wins, and no offer.
		scn.FreepsResolveSkirmish(merry);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// The Guard's 7 against the Chieftain's 7: the tie is the minion's.  The [rohan] Man loses and lives, and
		// there is no offer.
		scn.FreepsResolveSkirmish(guard);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(chieftain));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInHand(unyielding);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void IsNotOfferedWithNoRohanPossessionToDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var unyielding = scn.GetFreepsCard("unyielding");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var theoden = scn.GetFreepsCard("theoden");
		var aragorn = scn.GetFreepsCard("aragorn");
		var herugrim = scn.GetFreepsCard("herugrim");
		var sword = scn.GetFreepsCard("sword");
		var signalfire = scn.GetFreepsCard("signalfire");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		scn.MoveCompanionsToTable(guard1, guard2, theoden, aragorn);
		// A [rohan] artifact and a [gondor] possession, and no [rohan] possession.
		scn.AttachCardsTo(theoden, herugrim);
		scn.AttachCardsTo(aragorn, sword);
		scn.MoveCardsToHand(unyielding);
		assertInHand(unyielding);

		scn.StartGame();
		assertAttachedTo(herugrim, theoden);
		assertAttachedTo(sword, aragorn);
		scn.MoveMinionsToTable(savage1, savage2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard1, savage1}, new PhysicalCardImpl[]{guard2, savage2});

		// 6 against 5: a [rohan] Man wins, and with nothing to discard there is no offer.
		scn.FreepsResolveSkirmish(guard1);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage1));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());

		// With a [rohan] possession on the table, the second Guard's win brings the offer.
		scn.MoveCardsToSupportArea(signalfire);
		assertInZone(Zone.SUPPORT, signalfire);
		scn.FreepsResolveSkirmish(guard2);
		scn.FreepsPass();
		scn.ShadowPass();
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(unyielding));

		scn.FreepsChooseOptionalTrigger(unyielding);
		assertInDiscard(signalfire);
		scn.FreepsChooseCard(savage1);
		assertEquals(2, scn.GetWoundsOn(savage1));
		assertEquals(1, scn.GetWoundsOn(savage2));
		assertInDiscard(unyielding);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
