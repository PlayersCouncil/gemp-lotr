package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_06_094_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("hama", "6_94");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, vitality 3; valiant and nothing else
					put("guma", "4_277");       // Guma, Plains Farmer: a [rohan] Man ALLY, cost 1; a fellowship ability used only on purpose
					// Merry, Master Holbytla: a [rohan] Hobbit companion, a Man only while 3 [rohan] companions or
					// Eowyn can be spotted; his own abilities are used only on purpose
					put("merry", "103_85");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion, strength 8, vitality 4
					// Rider's Spear: a [rohan] hand weapon possession on a [rohan] Man; damage +1 only while bearer is mounted
					put("spear", "4_288");

					// The Witch-king, Lord of Angmar: [wraith] Nazgul, fierce, strength 14, vitality 4
					put("wk", "1_237");
					// The Pale Blade: [wraith] hand weapon possession on The Witch-king; its response needs a Free
					// Peoples condition, of which there is none
					put("blade", "1_221");
					// Black Steed: [wraith] mount possession on a Nazgul; +2 at a plains site
					put("steed", "1_208");
					// Ring of Rancor: [wraith] artifact (a Ring) on a Nazgul; a maneuver ability and a reconcile response
					put("ring", "9_44");
					// In Twilight: [wraith] condition on a [wraith] minion; its win trigger needs the bearer to win
					put("twilight", "19_34");
					// Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier", "1_271");
					put("soldier2", "1_271");
					// Banner of the Eye: [sauron] hand weapon possession on a [sauron] Orc, strength +1; its trigger
					// needs the bearer to win
					put("banner", "6_98");
					put("banner2", "6_98");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 only
					// Uruk-hai Sword: [isengard] hand weapon possession on an Uruk-hai, strength +2; its trigger needs
					// the bearer to win
					put("usword", "1_160");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HamaStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Háma, Doorward of Théoden
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: To play, spot a [rohan] Man.<br><b>Skirmish:</b> Exert Háma to discard a possession borne by a minion he is skirmishing.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("hama");

		assertEquals("Háma", card.getBlueprint().getTitle());
		assertEquals("Doorward of Théoden", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void CannotBePlayedBesideARohanHobbitAndAGondorManUntilARohanManAllyIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(merry, aragorn);
		scn.MoveCardsToHand(hama, guma);

		scn.StartGame();

		// A [rohan] companion who is no Man, and a Man who is not [rohan].
		assertFalse(scn.IsRace(merry, Race.MAN));
		assertInHand(hama);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(hama));

		scn.FreepsPlayCard(guma);
		assertInZone(Zone.SUPPORT, guma);

		assertTrue(scn.FreepsPlayAvailable(hama));
		scn.FreepsPlayCard(hama);
		assertInZone(Zone.FREE_CHARACTERS, hama);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanManCompanionLetsHimBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		scn.MoveCardsToHand(hama);

		scn.StartGame();

		assertInHand(hama);
		assertTrue(scn.FreepsPlayAvailable(hama));
		scn.FreepsPlayCard(hama);
		assertInZone(Zone.FREE_CHARACTERS, hama);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertsHamaToDiscardAPossessionBorneByAMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(hama, guard, aragorn);
		scn.AttachCardsTo(hama, spear);

		var wk = scn.GetShadowCard("wk");
		var blade = scn.GetShadowCard("blade");
		var steed = scn.GetShadowCard("steed");
		var ring = scn.GetShadowCard("ring");
		var twilight = scn.GetShadowCard("twilight");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var banner = scn.GetShadowCard("banner");
		var banner2 = scn.GetShadowCard("banner2");
		var savage = scn.GetShadowCard("savage");
		var usword = scn.GetShadowCard("usword");

		scn.StartGame();
		scn.MoveMinionsToTable(wk, soldier, soldier2, savage);
		scn.AttachCardsTo(wk, blade, steed, ring, twilight);
		scn.AttachCardsTo(soldier, banner);
		scn.AttachCardsTo(soldier2, banner2);
		scn.AttachCardsTo(savage, usword);
		assertAttachedTo(spear, hama);
		assertAttachedTo(blade, wk);
		assertAttachedTo(steed, wk);
		assertAttachedTo(ring, wk);
		assertAttachedTo(twilight, wk);
		assertAttachedTo(banner, soldier);
		assertAttachedTo(banner2, soldier2);
		assertAttachedTo(usword, savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{hama, wk}, new PhysicalCardImpl[]{guard, soldier2},
				new PhysicalCardImpl[]{aragorn, savage});
		// The Orc Soldier left over joins The Witch-king against Háma.
		scn.ShadowAssignToMinions(hama, soldier);

		// The Guard's skirmish, Háma waiting for his own: his ability is offered and paid for, and the Banner, borne
		// by a minion skirmishing another companion, is not discarded.
		scn.FreepsResolveSkirmish(guard);
		assertEquals(0, scn.GetWoundsOn(hama));
		assertTrue(scn.FreepsActionAvailable(hama));
		scn.FreepsUseCardAction(hama);
		assertEquals(1, scn.GetWoundsOn(hama));
		assertAttachedTo(banner2, soldier2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.PassSkirmishActions();
		// The second Soldier (8) beats the Guard (6); with no site controlled the Banner's win trigger is not offered (F-0708).
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(banner2));
		assertAttachedTo(banner2, soldier2);

		// His own skirmish, against The Witch-king and the first Soldier.
		scn.FreepsResolveSkirmish(hama);
		assertTrue(scn.FreepsActionAvailable(hama));
		scn.FreepsUseCardAction(hama);
		assertEquals(2, scn.GetWoundsOn(hama));

		// The possessions of the two minions skirmishing him, a Nazgul and an Orc: the Witch-king's hand weapon and
		// mount and the Soldier's Banner. Not the Witch-king's artifact or condition, not the second Soldier's
		// Banner (his skirmish is over), not the Uruk-hai Sword (its bearer waits for Aragorn), and not Háma's own
		// Spear.
		assertTrue(scn.FreepsHasCardChoiceAvailable(blade, steed, banner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(ring, twilight, banner2, usword, spear));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(steed);

		assertInDiscard(steed);
		assertAttachedTo(blade, wk);
		assertAttachedTo(banner, soldier);
		assertAttachedTo(ring, wk);
		assertAttachedTo(twilight, wk);
		assertAttachedTo(banner2, soldier2);
		assertAttachedTo(usword, savage);
		assertAttachedTo(spear, hama);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileHamaIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var hama = scn.GetFreepsCard("hama");
		scn.MoveCompanionsToTable(hama);
		var soldier = scn.GetShadowCard("soldier");
		var banner = scn.GetShadowCard("banner");

		scn.StartGame();
		scn.AddWoundsToChar(hama, 2);
		assertEquals(2, scn.GetWoundsOn(hama));
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, banner);
		assertAttachedTo(banner, soldier);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(hama, soldier);
		scn.FreepsResolveSkirmish(hama);

		// Vitality 3 with 2 wounds: Háma cannot exert, with a possession on the minion he is skirmishing.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(hama));
		assertAttachedTo(banner, soldier);
	}
}
