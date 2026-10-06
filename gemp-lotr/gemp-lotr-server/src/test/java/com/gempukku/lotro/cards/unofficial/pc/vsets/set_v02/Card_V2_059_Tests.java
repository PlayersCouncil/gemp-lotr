package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.MinionSiteNumberModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("grishnakh", "102_59");

					// Orc Chaser: a [sauron] Orc and tracker, site 6.  "The site number of each [sauron] Orc is -1."
					// Its other sentence makes it stronger while the Shadow player has initiative, and it is
					// never in a skirmish that is resolved.
					put("chaser1", "7_298");
					put("chaser2", "7_298");
					// Orc Soldier: a [sauron] Orc who is not unique and no tracker, cost 2, strength 7, site 6.
					// A skirmish ability that is never used.
					put("soldier1", "1_271");
					put("soldier2", "1_271");
					put("soldier3", "1_271");
					// Gothmog, Lieutenant of Morgul: a unique [sauron] Orc, site 4.  His text is about being
					// played and about sites the Shadow player controls; he is put on the table and there are none.
					put("gothmog", "10_88");
					// Goblin Patrol Troop: a [moria] Orc, site 4, no text.
					put("troop", "1_177");
					// Marsh Wight: a [sauron] Wraith, site 4.  Its text is about being played and about 4 burdens.
					put("wight", "102_61");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					put("guard", "1_7");        // Dwarf Guard: strength 4, vitality 2
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GrishnakhStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Grishnakh, Servant of Lugburz
		 * Unique: True
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 11
		 * Vitality: 3
		 * Site Number: 6
		 * Game Text: Tracker.
		 * The site number of each [sauron] orc is -3.
		 * Skirmish: Exert Grishnakh to make a skirmishing [sauron] Orc with a site number of 1 or less
		 * damage +1.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("grishnakh");

		assertEquals("Grishnakh", card.getBlueprint().getTitle());
		assertEquals("Servant of Lugburz", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.ORC, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.TRACKER));
		assertEquals(4, card.getBlueprint().getTwilightCost());
		assertEquals(11, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void IsPlayedAtHisOwnSiteNumberAndOnceInPlayMakesEachSauronOrcSiteNumberMinusThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var chaser = scn.GetShadowCard("chaser1");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var troop = scn.GetShadowCard("troop");
		var wight = scn.GetShadowCard("wight");
		scn.MoveCardsToHand(grishnakh, soldier2);

		scn.StartGame();
		scn.MoveMinionsToTable(chaser, soldier1, troop, wight);
		scn.SetTwilight(5);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertEquals(2, scn.GetCurrentSiteNumber());
		// 5 set + 1 companion + 2 for the site
		assertEquals(8, scn.GetTwilight());
		// The Orc Chaser's -1 is on every [sauron] Orc, itself included: 6 - 1.
		assertEquals(5, scn.GetMinionSiteNumber(chaser));
		assertEquals(5, scn.GetMinionSiteNumber(soldier1));
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(4, scn.GetMinionSiteNumber(wight));
		assertTrue(scn.HasKeyword(chaser, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(soldier1, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(wight, Keyword.ROAMING));
		assertTrue(scn.ShadowPlayAvailable(grishnakh));

		scn.ShadowPlayCard(grishnakh);
		// 4 to play + 2 roaming.  His own -3 does not apply to him while he is played: his site number is
		// 6 - 1 for the Chaser, at site 2.  With it he would have been site number 2, not roaming, and cost 4.
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, grishnakh);

		// Once in play it applies to each [sauron] Orc, himself included: 6 - 1 - 3.
		assertEquals(2, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(2, scn.GetMinionSiteNumber(chaser));
		assertEquals(2, scn.GetMinionSiteNumber(soldier1));
		assertFalse(scn.HasKeyword(grishnakh, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(chaser, Keyword.ROAMING));
		assertFalse(scn.HasKeyword(soldier1, Keyword.ROAMING));
		// The [moria] Orc and the [sauron] Wraith are where they were.
		assertEquals(4, scn.GetMinionSiteNumber(troop));
		assertEquals(4, scn.GetMinionSiteNumber(wight));
		assertTrue(scn.HasKeyword(troop, Keyword.ROAMING));
		assertTrue(scn.HasKeyword(wight, Keyword.ROAMING));

		// The Orc Soldier in hand is a [sauron] Orc too.  It would have cost 2 + 2 roaming before Grishnakh;
		// the 2 that are left now pay for it.
		assertTrue(scn.ShadowPlayAvailable(soldier2));
		scn.ShadowPlayCard(soldier2);
		assertEquals(0, scn.GetTwilight());
		assertInZone(Zone.SHADOW_CHARACTERS, soldier2);
		assertEquals(2, scn.GetMinionSiteNumber(soldier2));
		assertFalse(scn.HasKeyword(soldier2, Keyword.ROAMING));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishActionChoosesAmongSkirmishingSauronOrcsOfSiteNumberOneOrLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var chaser1 = scn.GetShadowCard("chaser1");
		var chaser2 = scn.GetShadowCard("chaser2");
		var soldier1 = scn.GetShadowCard("soldier1");
		var soldier2 = scn.GetShadowCard("soldier2");
		var soldier3 = scn.GetShadowCard("soldier3");
		var gothmog = scn.GetShadowCard("gothmog");
		var troop = scn.GetShadowCard("troop");
		var wight = scn.GetShadowCard("wight");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, chaser1, chaser2, soldier1, soldier2, soldier3, gothmog, troop, wight);
		// Grishnakh and the two Chasers take 5 from each [sauron] Orc.  A [moria] Orc and a [sauron] Wraith are
		// brought to site number 1 as well (4 - 3), and one Orc Soldier is held one step above the others.
		scn.ApplyAdHocModifier(new MinionSiteNumberModifier(null, Culture.MORIA, null, -3));
		scn.ApplyAdHocModifier(new MinionSiteNumberModifier(null, Race.WRAITH, null, -3));
		scn.ApplyAdHocModifier(new MinionSiteNumberModifier(null, soldier2, null, 1));

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		// Aragorn fights Grishnakh, Gothmog, two Orc Soldiers, the Goblin Patrol Troop and the Marsh Wight.
		// The Dwarf Guard will fight the third Orc Soldier afterwards.  The Chasers fight nobody.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, gothmog },
				new PhysicalCardImpl[] { guard, soldier3 }
		);
		scn.ShadowAssignToMinions(aragorn, grishnakh, soldier1, soldier2, troop, wight);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// 6 - 5
		assertEquals(1, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(1, scn.GetMinionSiteNumber(soldier1));
		assertEquals(1, scn.GetMinionSiteNumber(soldier3));
		assertEquals(1, scn.GetMinionSiteNumber(chaser1));
		// 4 - 5 is below zero, which is zero
		assertEquals(0, scn.GetMinionSiteNumber(gothmog));
		// 6 - 5 + 1
		assertEquals(2, scn.GetMinionSiteNumber(soldier2));
		assertEquals(1, scn.GetMinionSiteNumber(troop));
		assertEquals(1, scn.GetMinionSiteNumber(wight));
		assertTrue(scn.IsCharSkirmishing(soldier2));
		assertTrue(scn.IsCharSkirmishing(troop));
		assertTrue(scn.IsCharSkirmishing(wight));
		assertFalse(scn.IsCharSkirmishing(soldier3));
		assertTrue(scn.IsCharAssigned(soldier3));
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetKeywordCount(gothmog, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(grishnakh));

		scn.ShadowUseCardAction(grishnakh);
		assertEquals(1, scn.GetWoundsOn(grishnakh));
		// Himself, a plain Orc at exactly 1 and a unique one below it.
		assertTrue(scn.ShadowHasCardChoiceAvailable(grishnakh, soldier1, gothmog));
		// Not the Orc at 2, the Orc of another culture, the [sauron] minion that is no Orc, the Orc whose
		// skirmish has not begun, nor the Orcs in no skirmish at all.
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(soldier2, troop, wight, soldier3, chaser1, chaser2));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(gothmog);
		assertEquals(1, scn.GetKeywordCount(gothmog, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(grishnakh, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier1, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier2, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(troop, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(wight, Keyword.DAMAGE));
		assertEquals(1, scn.GetWoundsOn(grishnakh));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionExertsGrishnakhForDamagePlusOneThatLastsForThatSkirmishAndIsNotOfferedOnceHeIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var chaser1 = scn.GetShadowCard("chaser1");
		var chaser2 = scn.GetShadowCard("chaser2");
		var soldier1 = scn.GetShadowCard("soldier1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(aragorn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, chaser1, chaser2, soldier1);

		// Not an assignment action.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		assertFalse(scn.ShadowActionAvailable(grishnakh));
		scn.ShadowPass();

		// Aragorn fights Grishnakh alone; the Dwarf Guard will fight the Orc Soldier afterwards.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, grishnakh },
				new PhysicalCardImpl[] { guard, soldier1 }
		);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// 6 - 3 of his own - 1 for each Chaser: Grishnakh is himself a skirmishing [sauron] Orc of site
		// number 1, and the only one, so he is chosen without asking.
		assertEquals(1, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetKeywordCount(grishnakh, Keyword.DAMAGE));
		assertTrue(scn.ShadowActionAvailable(grishnakh));

		scn.ShadowUseCardAction(grishnakh);
		assertEquals(1, scn.GetWoundsOn(grishnakh));
		assertEquals(1, scn.GetKeywordCount(grishnakh, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier1, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.FreepsPass();
		scn.ShadowUseCardAction(grishnakh);
		assertEquals(2, scn.GetWoundsOn(grishnakh));
		assertEquals(2, scn.GetKeywordCount(grishnakh, Keyword.DAMAGE));

		// Vitality 3 with 2 wounds: exhausted, so he can no longer exert.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(grishnakh));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		scn.ShadowPass();
		// Grishnakh's 11 beats Aragorn's 8: 1 wound for losing + 2 for the two damage bonuses.
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// In the next skirmish of the same turn the bonuses are gone.
		scn.FreepsResolveSkirmish(guard);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishing(soldier1));
		assertEquals(0, scn.GetKeywordCount(grishnakh, Keyword.DAMAGE));
		assertEquals(2, scn.GetWoundsOn(grishnakh));
	}

	@Test
	public void SkirmishActionIsOfferedWithNoOrcOfSiteNumberOneOrLessAndThenOnlyExertsGrishnakh() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var grishnakh = scn.GetShadowCard("grishnakh");
		var chaser = scn.GetShadowCard("chaser1");
		var soldier = scn.GetShadowCard("soldier1");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(grishnakh, chaser, soldier);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, soldier);
		scn.ShadowAssignToMinions(aragorn, grishnakh, chaser);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		// 6 - 3 - 1: every [sauron] Orc in the skirmish is one step short.
		assertEquals(2, scn.GetMinionSiteNumber(grishnakh));
		assertEquals(2, scn.GetMinionSiteNumber(chaser));
		assertEquals(2, scn.GetMinionSiteNumber(soldier));
		assertEquals(0, scn.GetWoundsOn(grishnakh));
		// The exertion is a cost he can pay, so the action is there to be used.
		assertTrue(scn.ShadowActionAvailable(grishnakh));

		scn.ShadowUseCardAction(grishnakh);
		assertEquals(1, scn.GetWoundsOn(grishnakh));
		assertEquals(0, scn.GetKeywordCount(grishnakh, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(chaser, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(soldier, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
