package com.gempukku.lotro.cards.official.set15;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.VitalityModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_15_177_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("rager", "15_177");
					put("rager2", "15_177");

					// Tyrannical Uruk: an [uruk-hai] Uruk-hai, strength 14, vitality 3; damage +1 and toil 1 (a play
					// matter) only.
					put("tyrannical", "11_204");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, vitality 2, damage +1 only.
					put("guard", "10_81");
					// Hunt Them Down!: [isengard] Maneuver event, twilight 3: make an Uruk-hai fierce until the
					// regroup phase.
					put("hunt", "1_126");

					// Gimli, Son of Gloin: strength 6, vitality 3.  A tie goes to the Shadow side, so the Rager's 6
					// beats him, and is not double his strength.  His skirmish ability is never used.
					put("gimli", "1_13");
					// Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4.  His ability is never used.
					put("aragorn", "1_89");
					// Defend It and Hope: [gondor] Maneuver event: exert a [gondor] Man to wound a minion (twice if an
					// Uruk-hai).
					put("defend", "4_115");
					// Long I Fell, with Gandalf to spot: a Skirmish event whose prevention the Shadow player may buy by
					// having the Free Peoples player wound a minion of his choice, in a skirmish or not.
					put("gandalf", "1_72");
					put("fell", "4_97");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void UrukVillageRagerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 15
		 * Name: Uruk Village Rager
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 6
		 * Vitality: 1
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>Each time this minion wins a skirmish, take control of a site.<br>While you control a site, this minion cannot take wounds (except during the skirmish phase).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("rager");

		assertEquals("Uruk Village Rager", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(1, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WinningASkirmishTakesControlOfASiteAndThenItCannotBeWoundedInALaterManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveCardsToHand(defend);

		var rager = scn.GetShadowCard("rager");
		var rager2 = scn.GetShadowCard("rager2");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		// Player 1's second turn: both markers on site 2, and site 1 behind them.
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(rager, rager2, tyrannical, guard);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, rager);
		// The second copy, the Tyrannical Uruk and the Guard stay out of the skirmishes.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetShadowControlledSiteCount());

		// The Rager's 6 ties Gimli's 6, and wins.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertTrue(scn.ShadowControls(site1));
		// One site, for the copy that won only.
		assertEquals(1, scn.GetShadowControlledSiteCount());

		// A second move, and in the maneuver phase at site 4 the Rager cannot be wounded; the other minions can.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToPhase(Phase.MANEUVER);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(tyrannical, guard));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(rager, rager2));
		scn.FreepsChooseCard(tyrannical);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(2, scn.GetWoundsOn(tyrannical));
		assertInZone(Zone.SHADOW_CHARACTERS, rager, rager2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void LosingASkirmishTakesNoSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var rager = scn.GetShadowCard("rager");

		scn.StartGame();
		scn.SkipToSite(2);
		var site1 = scn.GetSite(1);
		scn.MoveMinionsToTable(rager);
		// Vitality raised by hand only so that the Rager lives through the wound of the skirmish it loses.
		scn.ApplyAdHocModifier(new VitalityModifier(null, rager, null, 1));
		assertEquals(2, scn.GetVitality(rager));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, rager);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's 8 beats the Rager's 6.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(rager));
		assertInZone(Zone.SHADOW_CHARACTERS, rager);
		assertFalse(scn.ShadowControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningAgainInTheFierceRoundTakesASecondSite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var rager = scn.GetShadowCard("rager");
		var hunt = scn.GetShadowCard("hunt");
		scn.MoveCardsToHand(hunt);

		scn.StartGame();
		// Player 1's third turn: both markers on site 3, and sites 1 and 2 behind them.
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.MoveMinionsToTable(rager);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(hunt);
		assertTrue(scn.HasKeyword(rager, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, rager);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.ShadowControls(site1));
		assertEquals(1, scn.GetShadowControlledSiteCount());

		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, rager);
		scn.FreepsResolveSkirmish(gimli);
		assertTrue(scn.InFierceRound());
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertTrue(scn.ShadowControls(site2));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		// Gimli's second loss, two more wounds, kills him.
		assertInZone(Zone.DEAD, gimli);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhileOnlyTheFreePeoplesPlayerControlsASiteItCanBeWoundedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend);

		var rager = scn.GetShadowCard("rager");
		var tyrannical = scn.GetShadowCard("tyrannical");

		scn.StartGame();
		scn.SkipToSite(2);
		// Site 1 the Free Peoples player took on a turn of the other player's.
		scn.FreepsTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(rager, tyrannical);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(defend);
		assertTrue(scn.FreepsHasCardChoiceAvailable(rager, tyrannical));
		scn.FreepsChooseCard(rager);

		// Vitality 1: the wound kills it.
		assertInZone(Zone.DISCARD, rager);
		assertEquals(0, scn.GetWoundsOn(tyrannical));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void DuringTheSkirmishPhaseItCanBeWoundedThoughItIsInNoSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToHand(fell);

		var rager = scn.GetShadowCard("rager");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.SkipToSite(2);
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		scn.MoveMinionsToTable(rager, tyrannical, guard);
		scn.SkipToAssignments();
		// Aragorn skirmishes the Guard; the Rager is in no skirmish at all.
		scn.FreepsAssignToMinions(aragorn, guard);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(tyrannical));

		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();
		assertTrue(scn.FreepsHasCardChoiceAvailable(rager, tyrannical, guard));
		scn.FreepsChooseCard(rager);

		assertInZone(Zone.DISCARD, rager);
		assertEquals(0, scn.GetWoundsOn(tyrannical));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
