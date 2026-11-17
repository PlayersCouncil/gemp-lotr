package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_185_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fortitude", "11_185");
					put("tyrannical", "11_204");    // Tyrannical Uruk: [uruk-hai] Uruk-hai minion, Damage +1, Toil 1, no other text
					put("saruman", "17_116");       // Saruman, Master of the White Hand: [uruk-hai] minion who is a Wizard, no Uruk-hai;
					                                //   each companion resistance -1, a skirmish ability used only on purpose
					put("hounding", "11_188");      // Hounding Uruk: [uruk-hai] Uruk-hai, strength 5, vitality 2; +3 only beside an exhausted companion
					put("savage", "1_151");         // Uruk Savage: [isengard] Uruk-hai minion, strength 5, vitality 3, Damage +1 only
					put("roar", "17_113");          // Deathly Roar: [uruk-hai] support-area condition that holds [uruk-hai] tokens;
					                                //   it answers only a Free Peoples possession or artifact played and an [uruk-hai] win
					put("worthy", "12_160");        // Worthy of Mordor: [uruk-hai] support-area condition of cost 0; acts only at the start of regroup
					put("boromir", "1_97");         // Boromir, Son of Denethor: a [gondor] Man to exert for Defend It and Hope
					put("aragorn", "1_89");         // Aragorn, Ranger of the North: strength 8, to win a skirmish
					put("defend", "4_115");         // Defend It and Hope: Maneuver: exert a [gondor] Man to wound a minion once
					                                //   (or twice if that minion is an Uruk-hai)
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FortitudeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Fortitude
		 * Unique: True
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 4
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: When you play this condition, spot an [uruk-hai] minion to add 3 [uruk-hai] tokens here.<br><b>Response:</b> If an [uruk-hai] minion is about to take a wound, discard this condition or remove an [uruk-hai] token from here to prevent that.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fortitude");

		assertEquals("Fortitude", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(4, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedBesideAnUrukhaiMinionItTakesThreeTokensAndAnotherConditionPlayedAfterTakesNone() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortitude = scn.GetShadowCard("fortitude");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var roar = scn.GetShadowCard("roar");
		var worthy = scn.GetShadowCard("worthy");
		scn.MoveCardsToHand(fortitude, worthy);
		scn.MoveCardsToSupportArea(roar);
		scn.AddTokensToCard(roar, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(tyrannical);
		scn.SetTwilight(10);

		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(0, scn.GetCultureTokensOn(fortitude));
		scn.FreepsPass();

		scn.ShadowPlayCard(fortitude);
		assertInZone(Zone.SUPPORT, fortitude);
		assertEquals(3, scn.GetCultureTokensOn(fortitude));
		// "Here": the other [uruk-hai] condition keeps its one token.
		assertEquals(1, scn.GetCultureTokensOn(roar));

		// Another [uruk-hai] condition played once this one is in play adds no token anywhere.
		scn.ShadowPlayCard(worthy);
		assertInZone(Zone.SUPPORT, worthy);
		assertEquals(3, scn.GetCultureTokensOn(fortitude));
		assertEquals(0, scn.GetCultureTokensOn(worthy));
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukhaiMinionWhoIsNoUrukhaiIsEnoughToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortitude = scn.GetShadowCard("fortitude");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCardsToHand(fortitude);

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertEquals(0, scn.GetCultureTokensOn(fortitude));
		scn.ShadowPlayCard(fortitude);
		assertEquals(3, scn.GetCultureTokensOn(fortitude));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void PlayedWithOnlyAnIsengardUrukhaiAndAnUrukhaiConditionItIsPlayedAndTakesNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortitude = scn.GetShadowCard("fortitude");
		var savage = scn.GetShadowCard("savage");
		var roar = scn.GetShadowCard("roar");
		scn.MoveCardsToHand(fortitude);
		scn.MoveCardsToSupportArea(roar);
		scn.AddTokensToCard(roar, 1);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SetTwilight(10);

		assertEquals(1, scn.GetCultureTokensOn(roar));
		scn.FreepsPass();

		// No [uruk-hai] minion: the Savage is an [isengard] Uruk-hai, Deathly Roar an [uruk-hai] card that is no minion.
		scn.ShadowPlayCard(fortitude);
		assertInZone(Zone.SUPPORT, fortitude);
		assertEquals(0, scn.GetCultureTokensOn(fortitude));
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void EachWoundOnAnUrukhaiMinionIsPreventedForATokenOrForThisConditionWhenBothCanBePaid() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortitude = scn.GetShadowCard("fortitude");
		var tyrannical = scn.GetShadowCard("tyrannical");
		var boromir = scn.GetFreepsCard("boromir");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend);
		scn.MoveCardsToSupportArea(fortitude);
		scn.AddTokensToCard(fortitude, 3);
		assertInZone(Zone.SUPPORT, fortitude);
		assertEquals(3, scn.GetCultureTokensOn(fortitude));

		scn.StartGame();
		scn.MoveMinionsToTable(tyrannical);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(3, scn.GetCultureTokensOn(fortitude));
		assertEquals(0, scn.GetWoundsOn(tyrannical));

		// Two wounds on the Uruk-hai, one at a time.  The only minion is chosen unasked.
		scn.FreepsPlayCard(defend);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fortitude));
		scn.ShadowAcceptOptionalTrigger();
		// Both costs can be paid, so the Shadow player is asked which.
		assertTrue(scn.ShadowChoiceAvailable("Discard"));
		assertTrue(scn.ShadowChoiceAvailable("token"));
		scn.ShadowChoose("token");
		assertEquals(2, scn.GetCultureTokensOn(fortitude));
		assertEquals(0, scn.GetWoundsOn(tyrannical));

		// The second wound: this time the condition is discarded.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fortitude));
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.ShadowChoiceAvailable("token"));
		scn.ShadowChoose("Discard");
		assertInDiscard(fortitude);
		assertEquals(0, scn.GetWoundsOn(tyrannical));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoTokenHereThisConditionIsDiscardedUnaskedToPreventAWoundOnAnUrukhaiMinionWhoIsNoUrukhai() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortitude = scn.GetShadowCard("fortitude");
		var saruman = scn.GetShadowCard("saruman");
		var roar = scn.GetShadowCard("roar");
		var boromir = scn.GetFreepsCard("boromir");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(defend);
		scn.MoveCardsToSupportArea(fortitude, roar);
		scn.AddTokensToCard(roar, 1);
		assertInZone(Zone.SUPPORT, fortitude, roar);
		assertEquals(0, scn.GetCultureTokensOn(fortitude));
		assertEquals(1, scn.GetCultureTokensOn(roar));

		scn.StartGame();
		scn.MoveMinionsToTable(saruman);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(fortitude));
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertEquals(0, scn.GetWoundsOn(saruman));

		// Saruman is no Uruk-hai: one wound.
		scn.FreepsPlayCard(defend);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fortitude));
		scn.ShadowAcceptOptionalTrigger();
		assertInDiscard(fortitude);
		assertEquals(0, scn.GetWoundsOn(saruman));
		// "From here": the token on the other [uruk-hai] condition is not taken.
		assertEquals(1, scn.GetCultureTokensOn(roar));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void OfTwoMinionsLosingOneSkirmishOnlyTheUrukhaiMinionIsSaved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fortitude = scn.GetShadowCard("fortitude");
		var hounding = scn.GetShadowCard("hounding");
		var savage = scn.GetShadowCard("savage");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(fortitude);
		scn.AddTokensToCard(fortitude, 3);
		assertInZone(Zone.SUPPORT, fortitude);
		assertEquals(3, scn.GetCultureTokensOn(fortitude));

		scn.StartGame();
		scn.MoveMinionsToTable(hounding, savage);
		// A strength that only arranges the board: 8 + 3 = 11 against 5 + 5.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 3));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, hounding);
		scn.ShadowAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(11, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetWoundsOn(hounding));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(3, scn.GetCultureTokensOn(fortitude));

		scn.PassSkirmishActions();
		// Both losers are about to take their wound together; only the [uruk-hai] one is saved.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(fortitude));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChoose("token");
		assertEquals(2, scn.GetCultureTokensOn(fortitude));
		assertEquals(0, scn.GetWoundsOn(hounding));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
