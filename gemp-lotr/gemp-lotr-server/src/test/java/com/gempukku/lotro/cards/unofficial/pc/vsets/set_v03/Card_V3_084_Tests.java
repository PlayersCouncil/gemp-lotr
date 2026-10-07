package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v03;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.AddKeywordModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V3_084_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("iamnoman", "103_84");
					put("haldir", "102_8");      // Haldir, Naith Commander: valiant Elf companion, strength 6, vitality 3; his text needs a ranged weapon or an [elven] possession
					put("scout", "5_90");        // Rohirrim Scout: valiant [rohan] Man companion, vitality 3; a regroup ability never used here
					put("eowyn", "5_122");       // Éowyn, Daughter of Éomund: valiant, strength 6, vitality 3; a maneuver ability never used here
					put("maiden", "7_229");      // Éowyn, Restless Maiden: an Éowyn who is not valiant; her ability needs a possession
					put("aragorn", "1_89");      // Aragorn, Ranger of the North: not valiant, strength 8
					put("rider", "4_286");       // Rider of Rohan: a bystander; his ability needs a mount
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: ally, strength 8, vitality 4, at home at site 3
					put("beacon", "103_35");     // War-beacon: a Free Peoples possession in the support area, there to be hindered

					put("troop", "1_177");       // Goblin Patrol Troop: [moria], strength 13, vitality 3, no text
					put("savage", "1_151");      // Uruk Savage: [isengard], strength 5, vitality 3, damage +1
					put("soldier", "1_271");     // Orc Soldier: [sauron], strength 7, vitality 2; a skirmish ability never used here
					put("runner", "1_178");      // Goblin Runner: there to be hindered
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void IAmNoManStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V3
		 * Name: I Am No Man
		 * Unique: false
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Exert a skirmishing valiant companion twice (or spot Eowyn in a skirmish) to make a skirmishing minion strength -1 for each hindered or wounded card you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("iamnoman");

		assertEquals("I Am No Man", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheSkirmishingValiantCompanionTwiceAndWeakensAMinionByOneForEachHinderedOrWoundedCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var beacon = scn.GetFreepsCard("beacon");
		scn.MoveCompanionsToTable(haldir, aragorn, rider);
		scn.MoveCardsToSupportArea(beacon);
		scn.MoveCardsToHand(iamnoman);
		// A wounded companion, a companion both wounded and hindered, and a hindered possession.
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(rider, 1);
		scn.HinderCard(rider, beacon);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, soldier, runner);
		// A minion with two wounds and a hindered minion.
		scn.AddWoundsToChar(savage, 2);
		scn.HinderCard(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haldir, troop}, new PhysicalCardImpl[]{aragorn, soldier});
		scn.ShadowAssignToMinions(haldir, savage);
		scn.FreepsResolveSkirmish(haldir);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.FreepsPlayAvailable(iamnoman));

		scn.FreepsPlayCard(iamnoman);

		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(2, scn.GetWoundsOn(haldir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		// Both minions fighting Haldir are offered, of two cultures.
		assertTrue(scn.FreepsHasCardChoiceAvailable(troop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner, haldir, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(troop);

		// Six cards: Haldir (two wounds, one card), Aragorn, the Rider (wounded and hindered, one card), the
		// Uruk Savage (two wounds, one card), the hindered War-beacon and the hindered Goblin Runner.
		assertEquals(13 - 6, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(soldier));
		assertInZone(Zone.DISCARD, iamnoman);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithEowynInTheSkirmishNobodyExertsAndTheMinionIsWeakenedForThatSkirmishOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var rider = scn.GetFreepsCard("rider");
		var beacon = scn.GetFreepsCard("beacon");
		scn.MoveCompanionsToTable(eowyn, aragorn, rider);
		scn.MoveCardsToSupportArea(beacon);
		scn.MoveCardsToHand(iamnoman);
		scn.AddWoundsToChar(aragorn, 1);
		scn.HinderCard(beacon);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// Made fierce, so that the Troop fights a second skirmish before the regroup phase.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, troop, null, Keyword.FIERCE));
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(eowyn, troop);

		int twilight = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(13, scn.GetStrength(troop));

		scn.FreepsPlayCard(iamnoman);

		// Éowyn is valiant and could have exerted twice, but with her in the skirmish she is spotted instead.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(eowyn));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(rider));
		// Two cards: wounded Aragorn and the hindered War-beacon.
		assertEquals(13 - 2, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// A card wounded afterwards does not deepen the loss.
		scn.AddWoundsToChar(rider, 1);
		assertEquals(13 - 2, scn.GetStrength(troop));

		scn.ShadowPass();
		scn.FreepsPass();

		// Strength 6 against 11: Éowyn loses the skirmish.
		assertEquals(1, scn.GetWoundsOn(eowyn));

		// The Troop's fierce skirmish, against Aragorn. The skirmish the event was played in is over, and the
		// Troop has its strength back though the regroup phase has not come.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignAndResolve(aragorn, troop);
		assertTrue(scn.IsCharSkirmishing(aragorn));
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWhileEowynAndAValiantCompanionOnlyWaitThenEowynFightingPaysForItThoughSheCannotExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var maiden = scn.GetFreepsCard("maiden");
		var haldir = scn.GetFreepsCard("haldir");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(maiden, haldir, aragorn);
		scn.MoveCardsToHand(iamnoman);
		// This Éowyn is not valiant, and with vitality 3 and two wounds she cannot exert.
		scn.AddWoundsToChar(maiden, 2);

		var troop = scn.GetShadowCard("troop");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, savage, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, soldier},
				new PhysicalCardImpl[]{maiden, troop},
				new PhysicalCardImpl[]{haldir, savage});

		// Aragorn, who is not valiant, fights; Éowyn and valiant Haldir are assigned to skirmishes that have
		// not begun.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssigned(maiden));
		assertTrue(scn.IsCharAssigned(haldir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(iamnoman));
		// Strength 8 against 7: the Orc Soldier takes a wound and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(soldier));

		scn.FreepsResolveSkirmish(maiden);
		assertTrue(scn.FreepsPlayAvailable(iamnoman));

		scn.FreepsPlayCard(iamnoman);

		assertEquals(2, scn.GetWoundsOn(maiden));
		assertEquals(0, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		// Two cards: Éowyn and the Orc Soldier.
		assertEquals(13 - 2, scn.GetStrength(troop));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableWhenTheSkirmishingValiantCompanionCanExertOnlyOnce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var haldir = scn.GetFreepsCard("haldir");
		var scout = scn.GetFreepsCard("scout");
		scn.MoveCompanionsToTable(haldir, scout);
		scn.MoveCardsToHand(iamnoman);
		// Vitality 3 with a wound: one exertion left.
		scn.AddWoundsToChar(haldir, 1);

		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{haldir, savage}, new PhysicalCardImpl[]{scout, soldier});

		// The Scout, valiant and unwounded, is not the one fighting.
		scn.FreepsResolveSkirmish(haldir);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(iamnoman));
		// Strength 6 against 5: the Uruk Savage takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		scn.FreepsResolveSkirmish(scout);
		assertEquals(0, scn.GetWoundsOn(scout));
		assertTrue(scn.FreepsPlayAvailable(iamnoman));

		scn.FreepsPlayCard(iamnoman);

		assertEquals(2, scn.GetWoundsOn(scout));
		assertEquals(1, scn.GetWoundsOn(haldir));
		// Three cards: Haldir, the Uruk Savage and now the Scout.
		assertEquals(7 - 3, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ASkirmishingValiantAllyIsNotACompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);
		// No ally is valiant in print, though one may be made so (Flame of Calenhad gives a Man's keywords to
		// the player's other Men).
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, elrond, null, Keyword.VALIANT));

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToHand(iamnoman);
		scn.MoveMinionsToTable(savage);
		// The fellowship moves to site 3, Elrond's home, where he may skirmish.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignAndResolve(elrond, savage);

		assertTrue(scn.IsCharSkirmishing(elrond));
		assertTrue(scn.HasKeyword(elrond, Keyword.VALIANT));
		// Vitality 4 and unwounded: he could exert twice.
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(iamnoman));
	}

	@Test
	public void AMinionThatIsNotInTheSkirmishIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var iamnoman = scn.GetFreepsCard("iamnoman");
		var eowyn = scn.GetFreepsCard("eowyn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var beacon = scn.GetFreepsCard("beacon");
		scn.MoveCompanionsToTable(eowyn, aragorn);
		scn.MoveCardsToSupportArea(beacon);
		scn.MoveCardsToHand(iamnoman);
		scn.HinderCard(beacon);

		var troop = scn.GetShadowCard("troop");
		var soldier = scn.GetShadowCard("soldier");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, soldier);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{eowyn, troop}, new PhysicalCardImpl[]{aragorn, soldier});
		scn.FreepsResolveSkirmish(eowyn);

		scn.FreepsPlayCard(iamnoman);

		// The Orc Soldier is assigned to Aragorn, whose skirmish has not begun: it is not a skirmishing minion,
		// so there is nothing to choose between and the Troop is the one weakened (Q-0095).
		// One card: the hindered War-beacon.
		assertEquals(13 - 1, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(soldier));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
