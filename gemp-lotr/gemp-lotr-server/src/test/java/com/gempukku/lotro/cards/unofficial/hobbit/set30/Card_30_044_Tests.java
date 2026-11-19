package com.gempukku.lotro.cards.unofficial.hobbit.set30;

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

public class Card_30_044_Tests
{

	// The Hobbit table: the Hobbit path, no card in the Ring slot, and this Bilbo as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("oin", "30_16");            // Óin, Knowledgeable Healer: [dwarven] follower, Aid - Discard a [dwarven] card from hand; heals his bearer as he is transferred
					put("gwaihir", "31_15");        // Gwaihir, Lord of the Eagles: [gandalf] follower, Aid - Discard a [gandalf] card from hand; a skirmish ability not used
					put("bifur", "30_5");           // Bifur, Inarticulate: [dwarven] follower, Aid - Exert a companion; a fierce-win trigger not reached
					put("warhammer", "33_7");       // Dwarven Warhammer: on a [dwarven] companion, "Maneuver: Discard 2 cards from hand to attach a [dwarven] follower from your support area to bearer (without paying the aid cost)"
					put("robin", "18_111");         // Robin Smallburrow, Shirriff Cock-Robin: [shire] follower, Aid - (2); "Each time you attach this follower to a [shire] companion, you may discard X cards from hand to draw X cards"
					put("balin", "30_2");           // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7, vitality 3; a fellowship ability only
					put("battlefury", "30_3");      // Battle Fury: a [dwarven] skirmish event, here only a [dwarven] card in hand
					put("tome", "30_20");           // To me! O my kinsfolk!: a [dwarven] fellowship event, here only a card in hand
					put("wizard", "30_23");         // A Wizard Is Never Late: a [gandalf] fellowship event, here only a [gandalf] card in hand
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, strength 5; an assignment ability that wants threats or a companion of cost 0 who is not the Ring-bearer
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: [gollum] minion who is no Gollum, strength 8, fierce
					put("smaug", "30_66");          // Smaug, The Golden: [smaug] minion, strength 17, fierce; discarded if not at a mountain
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5, vitality 1; a text on being played, never reached
				}},
				null,
				"30_44",
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bilbo, Collector of Treasures
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 0
		 * Type: Companion
		 * Subtype: Hobbit
		 * Strength: 3
		 * Vitality: 4
		 * Resistance: 8
		 * Can be the Ring-bearer: True
		 * Game Text: <b>Burglar</b>.
		 * 	When you discard a card from hand to transfer a follower, you may draw 2 cards.
		 * 	<b>Skirmish:</b> If Bilbo is skirmishing Smaug or Gollum, add a doubt to make Bilbo strength +3 (limit +3).
		 */

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("30_44", card.getBlueprintId());
		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Collector of Treasures", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.HOBBIT, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.BURGLAR));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(3, card.getBlueprint().getStrength());
		assertEquals(4, card.getBlueprint().getVitality());
		assertEquals(8, card.getBlueprint().getResistance());
		assertTrue(card.getBlueprint().canStartWithRing());
	}

	@Test
	public void EachCardDiscardedFromHandForAFollowersAidMayDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var oin = scn.GetFreepsCard("oin");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var battlefury = scn.GetFreepsCard("battlefury");
		var wizard = scn.GetFreepsCard("wizard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(oin, gwaihir);
		scn.MoveCardsToHand(battlefury, wizard);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, oin, gwaihir);
		assertInZone(Zone.HAND, battlefury, wizard);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(oin, "Aid"));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir, "Aid"));
		int deck = scn.GetFreepsDeckCount();
		assertEquals(2, scn.GetFreepsHandCount());

		// Óin's Aid: the one [dwarven] card in hand is discarded, the offer is made as the cost is paid, and Óin
		// then goes to Balin.
		scn.FreepsChooseOptionalTrigger(oin, "Aid");
		assertInZone(Zone.DISCARD, battlefury);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsChooseOptionalTrigger(bilbo);
		// 2 in hand - 1 discarded + 2 drawn
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		scn.FreepsChooseCard(balin);
		assertAttachedTo(oin, balin);

		// Gwaihir's Aid: the one [gandalf] card in hand is discarded.  The offer comes again, and is declined;
		// Gwaihir goes to Bilbo.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(gwaihir, "Aid"));
		scn.FreepsChooseOptionalTrigger(gwaihir, "Aid");
		assertInZone(Zone.DISCARD, wizard);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsChooseCard(bilbo);
		assertAttachedTo(gwaihir, bilbo);

		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AFollowersAidPaidByAnExertionDrawsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var battlefury = scn.GetFreepsCard("battlefury");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(bifur);
		scn.MoveCardsToHand(battlefury);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bifur);
		assertInZone(Zone.HAND, battlefury);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetWoundsOn(balin));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bifur, "Aid"));
		scn.FreepsAcceptOptionalTrigger();
		// Bifur's Aid exerts a companion, then he goes to a companion.
		scn.FreepsChooseCard(balin);
		scn.FreepsChooseCard(balin);

		assertEquals(1, scn.GetWoundsOn(balin));
		assertAttachedTo(bifur, balin);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertInZone(Zone.HAND, battlefury);
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
	}

	@Test
	public void CardsDiscardedFromHandToAttachAFollowerByDwarvenWarhammerMayDrawTwoCardsOnce() throws DecisionResultInvalidException, CardNotFoundException {
		// Ketura (Q-0407): "'attach' was bad wording.  Given that internally it uses the 'transfer' action, then we
		// should treat Bilbo as responding to the warhammer".  The Warhammer's discard of 2 makes one offer for
		// the use, not one for each card.
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var oin = scn.GetFreepsCard("oin");
		var warhammer = scn.GetFreepsCard("warhammer");
		var battlefury = scn.GetFreepsCard("battlefury");
		var tome = scn.GetFreepsCard("tome");
		var wizard = scn.GetFreepsCard("wizard");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.AttachCardsTo(balin, warhammer);
		scn.MoveCardsToSupportArea(oin);
		scn.MoveCardsToHand(battlefury, tome, wizard);

		scn.StartGame();
		assertAttachedTo(warhammer, balin);
		assertInZone(Zone.SUPPORT, oin);
		assertInZone(Zone.HAND, battlefury, tome, wizard);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Óin's own Aid is declined.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(oin, "Aid"));
		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SUPPORT, oin);

		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsActionAvailable(warhammer));
		scn.FreepsUseCardAction(warhammer);
		scn.FreepsChooseCards(tome, wizard);
		assertInZone(Zone.DISCARD, tome, wizard);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsChooseOptionalTrigger(bilbo);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));

		assertAttachedTo(oin, balin);
		// 3 in hand - 2 discarded + 2 drawn
		assertEquals(3, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CardsAFollowerHasDiscardedFromHandAfterItsTransferDrawNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var robin = scn.GetFreepsCard("robin");
		var battlefury = scn.GetFreepsCard("battlefury");
		var tome = scn.GetFreepsCard("tome");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(robin);
		scn.MoveCardsToHand(battlefury, tome);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, robin);
		assertInZone(Zone.HAND, battlefury, tome);
		scn.MoveMinionsToTable(runner);
		scn.SkipToPhase(Phase.MANEUVER);

		// Robin's Aid is paid in twilight, and Bilbo, the one companion, bears him.
		int pool = scn.GetTwilight();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin, "Aid"));
		scn.FreepsChooseOptionalTrigger(robin, "Aid");
		assertEquals(pool + 2, scn.GetTwilight());
		assertAttachedTo(robin, bilbo);

		// Robin's own trigger: a card discarded from hand to draw one.  The follower has been transferred already,
		// and the card is discarded to draw, not to transfer him.
		int deck = scn.GetFreepsDeckCount();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(robin));
		scn.FreepsChooseOptionalTrigger(robin);
		scn.FreepsChooseCards(tome);
		assertInZone(Zone.DISCARD, tome);

		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		// 2 in hand - 1 discarded + 1 drawn by Robin
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());
	}

	@Test
	public void SkirmishActionAddsADoubtToMakeBilboStrength3MoreAgainstGollumUpToTheLimitInEachSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("gollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);
		// Fierce by hand, for a second skirmish of Bilbo's against him.
		scn.ApplyAdHocModifier(new AddKeywordModifier(null, gollum, null, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(bilbo));

		// At the limit the ability is still offered, its doubt paid for nothing.
		scn.ShadowPass();
		assertTrue(scn.FreepsActionAvailable(bilbo));
		scn.FreepsUseCardAction(bilbo);
		assertEquals(2, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(bilbo));

		// Bilbo 6 beats Gollum 5.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(gollum));
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());

		// The fierce skirmish is a phase of its own: the +3 is gone, and the limit is new.
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));
		scn.FreepsUseCardAction(bilbo);
		assertEquals(3, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionWorksAgainstGollumHalfAWraithHimself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var gollum = scn.GetShadowCard("wraithgollum");

		scn.StartGame();
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(0, scn.GetBurdens());
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionWorksAgainstSmaug() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var smaug = scn.GetShadowCard("smaug");

		// Erebor, site 7, is a mountain: Smaug stays there (he is discarded anywhere else, so he is laid there).
		scn.StartGame();
		scn.SkipToSite(6);
		scn.SkipToPhase(Phase.SHADOW);
		assertEquals(7, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(smaug);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, smaug);
		scn.FreepsResolveSkirmish(bilbo);

		int burdens = scn.GetBurdens();
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertEquals(burdens + 1, scn.GetBurdens());
		assertEquals(6, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionIsNotOfferedWhileBilboWaitsForHisSkirmishWithGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner}, new PhysicalCardImpl[]{bilbo, gollum});
		scn.FreepsResolveSkirmish(balin);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bilbo));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(bilbo));
	}

	@Test
	public void SkirmishActionIsNotOfferedAgainstAMinionWhoIsNoGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gollum = scn.GetShadowCard("gollum");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(balin);

		scn.StartGame();
		scn.MoveMinionsToTable(gollum, shelob);

		scn.SkipToAssignments();
		// Gollum is in play, assigned to Balin and waiting.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, gollum}, new PhysicalCardImpl[]{bilbo, shelob});
		scn.FreepsResolveSkirmish(bilbo);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bilbo));
	}
}
