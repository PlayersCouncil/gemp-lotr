package com.gempukku.lotro.cards.unofficial.hobbit.set30;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_30_043_Tests
{

	// The Hobbit table: the Hobbit path, no card in the Ring slot, and this Bilbo as the Ring-bearer.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("tome", "30_20");           // To me! O my kinsfolk!: [dwarven] fellowship event, cost 0, "Play a [dwarven] follower from your discard pile"
					put("tome2", "30_20");
					put("tome3", "30_20");
					put("king", "32_6");            // King Under the Mountain: [dwarven] fellowship event, cost 1, "Add a doubt to play a [dwarven] follower from your draw deck"
					put("elfsong", "1_39");         // Elf-song: [elven] fellowship event, "Spot an Elf to remove a burden"
					put("elrond", "30_21");         // Elrond, Elven Lord: [elven] Elf ally, the Elf to spot; a start-of-turn heal that has nobody to heal
					put("battlefury", "30_3");      // Battle Fury: [dwarven] SKIRMISH event, "Make a Dwarf character strength +3 (or +4 if he bears a [dwarven] follower)"
					put("bifur", "30_5");           // Bifur, Inarticulate: [dwarven] follower, cost 1, Aid - Exert a companion; a fierce-win trigger not reached
					put("oin", "30_16");            // Óin, Knowledgeable Healer: [dwarven] follower, cost 1, Aid - Discard a [dwarven] card from hand; heals his bearer as he is transferred
					put("dori", "30_8");            // Dori, Really a decent fellow: [dwarven] follower, cost 1, Aid - Exert Bilbo; a bar on overwhelming his bearer, never reached
					put("gwaihir", "31_15");        // Gwaihir, Lord of the Eagles: [gandalf] follower, Aid - Discard a [gandalf] card from hand (none is in hand)
					put("balin", "30_2");           // Balin, Brother of Dwalin: [dwarven] Dwarf companion, strength 7, vitality 3; a fellowship ability only
					put("gollum", "5_24");          // Gollum, Nasty Treacherous Creature: [gollum] minion, strength 5, vitality 4; a regroup ability only
					put("wraithgollum", "103_66");  // Gollum, Half a Wraith Himself: [wraith] minion, strength 5; an assignment ability that wants threats or a companion of cost 0 who is not the Ring-bearer
					put("shelob", "8_26");          // Shelob, Last Child of Ungoliant: [gollum] minion who is no Gollum, strength 8, fierce
					put("runner", "1_178");         // Goblin Runner: [moria] Orc, strength 5, vitality 1; a text on being played, never reached
					put("runner2", "1_178");
				}},
				null,
				"30_43",
				null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void BilboStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 30
		 * Name: Bilbo, Expert Burglar
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
		 * 	Each time you play a [dwarven] fellowship event, you may draw 2 cards.
		 * 	<b>Fellowship or Skirmish:</b> Discard a [dwarven] follower to make Bilbo strength +1 (or +2 if you spot Gollum) until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetRingBearer();

		assertEquals("30_43", card.getBlueprintId());
		assertEquals("Bilbo", card.getBlueprint().getTitle());
		assertEquals("Expert Burglar", card.getBlueprint().getSubtitle());
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
	public void EachDwarvenFellowshipEventPlayedMayDrawTwoCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var tome = scn.GetFreepsCard("tome");
		var king = scn.GetFreepsCard("king");
		var bifur = scn.GetFreepsCard("bifur");
		var oin = scn.GetFreepsCard("oin");
		scn.MoveCardsToHand(tome, king);
		scn.MoveCardsToDiscard(bifur);

		scn.StartGame();
		assertInZone(Zone.HAND, tome, king);
		assertInZone(Zone.DISCARD, bifur);
		assertInZone(Zone.DECK, oin);

		// King Under the Mountain first, its offer declined: Óin is taken from the draw deck before any card is
		// drawn at random, which could have drawn him into the hand.
		int deck = scn.GetFreepsDeckCount();
		assertEquals(0, scn.GetBurdens());
		scn.FreepsPlayCard(king);
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsDismissRevealedCards();
		scn.FreepsChooseCardBPFromSelection(oin);
		assertInZone(Zone.SUPPORT, oin);
		// 2 in hand - King Under the Mountain
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(deck - 1, scn.GetFreepsDeckCount());

		// A second, of other text: offered again, and taken.
		deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(tome);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		scn.FreepsChooseOptionalTrigger(bilbo);
		// To me! plays Bifur from the discard pile.
		assertInZone(Zone.SUPPORT, bifur);
		// 1 in hand - To me! + 2 drawn
		assertEquals(2, scn.GetFreepsHandCount());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void NoOfferOnceTheRuleOfFourIsSpent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var tome = scn.GetFreepsCard("tome");
		var tome2 = scn.GetFreepsCard("tome2");
		var tome3 = scn.GetFreepsCard("tome3");
		var bifur = scn.GetFreepsCard("bifur");
		var oin = scn.GetFreepsCard("oin");
		var dori = scn.GetFreepsCard("dori");
		scn.MoveCardsToHand(tome, tome2, tome3);
		scn.MoveCardsToDiscard(bifur, oin, dori);

		scn.StartGame();
		assertInZone(Zone.HAND, tome, tome2, tome3);
		assertInZone(Zone.DISCARD, bifur, oin, dori);

		// Two events, four cards drawn.
		scn.FreepsPlayCard(tome);
		scn.FreepsChooseOptionalTrigger(bilbo);
		scn.FreepsChooseCardBPFromSelection(bifur);
		scn.FreepsPlayCard(tome2);
		scn.FreepsChooseOptionalTrigger(bilbo);
		scn.FreepsChooseCardBPFromSelection(oin);
		// 3 in hand - 2 events + 4 drawn
		assertEquals(5, scn.GetFreepsHandCount());

		int deck = scn.GetFreepsDeckCount();
		scn.FreepsPlayCard(tome3);
		assertInZone(Zone.SUPPORT, dori);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
	}

	@Test
	public void AFollowerAnElvenFellowshipEventAndADwarvenSkirmishEventPlayedMakeNoOffer() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var elfsong = scn.GetFreepsCard("elfsong");
		var elrond = scn.GetFreepsCard("elrond");
		var battlefury = scn.GetFreepsCard("battlefury");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(bifur, elfsong, battlefury);

		scn.StartGame();
		assertInZone(Zone.HAND, bifur, elfsong, battlefury);
		int deck = scn.GetFreepsDeckCount();

		// A [dwarven] card that is no event.
		scn.FreepsPlayCard(bifur);
		assertInZone(Zone.SUPPORT, bifur);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A fellowship event of another culture.
		scn.AddBurdens(1);
		assertEquals(1, scn.GetBurdens());
		scn.FreepsPlayCard(elfsong);
		assertEquals(0, scn.GetBurdens());
		assertInZone(Zone.DISCARD, elfsong);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A [dwarven] event of another phase.
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(balin, runner);
		scn.FreepsResolveSkirmish(balin);
		scn.FreepsPlayCard(battlefury);
		assertEquals(10, scn.GetStrength(balin));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(0, scn.GetFreepsHandCount());
		assertEquals(deck, scn.GetFreepsDeckCount());
	}

	@Test
	public void FellowshipActionDiscardsADwarvenFollowerToMakeBilboStrength1MoreUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var oin = scn.GetFreepsCard("oin");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(bifur, gwaihir);
		scn.AttachCardsTo(balin, oin);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bifur, gwaihir);
		assertAttachedTo(oin, balin);
		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));

		scn.FreepsUseCardAction(bilbo);
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, oin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gwaihir, balin));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(bifur);

		assertInZone(Zone.DISCARD, bifur);
		assertAttachedTo(oin, balin);
		assertEquals(4, scn.GetStrength(bilbo));

		scn.MoveMinionsToTable(runner, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{balin, runner}, new PhysicalCardImpl[]{bilbo, runner2});
		scn.FreepsResolveSkirmish(balin);
		scn.PassSkirmishActions();

		// Still +1 in a skirmish after another has been fought.
		scn.FreepsResolveSkirmish(bilbo);
		assertEquals(4, scn.GetStrength(bilbo));
		// Bilbo 4 loses to the Runner's 5.
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetStrength(bilbo));
	}

	@Test
	public void SkirmishActionDiscardsADwarvenFollowerToMakeBilboStrength1MoreUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var oin = scn.GetFreepsCard("oin");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var runner = scn.GetShadowCard("runner");
		var shelob = scn.GetShadowCard("shelob");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(bifur, gwaihir);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bifur, gwaihir);
		scn.MoveMinionsToTable(runner, shelob);

		scn.SkipToAssignments();
		scn.AttachCardsTo(balin, oin);
		assertAttachedTo(oin, balin);
		// Shelob, a [gollum] minion who is no Gollum, is in play.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, runner}, new PhysicalCardImpl[]{balin, shelob});
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(3, scn.GetStrength(bilbo));
		assertTrue(scn.FreepsActionAvailable(bilbo));
		scn.FreepsUseCardAction(bilbo);
		assertTrue(scn.FreepsHasCardChoiceAvailable(bifur, oin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gwaihir, balin));
		scn.FreepsChooseCard(oin);

		assertInZone(Zone.DISCARD, oin);
		assertInZone(Zone.SUPPORT, bifur);
		assertEquals(4, scn.GetStrength(bilbo));
		// Bilbo 4 loses to the Runner's 5.
		scn.PassSkirmishActions();

		// Still +1 in the next skirmish.
		scn.FreepsResolveSkirmish(balin);
		assertEquals(4, scn.GetStrength(bilbo));
		scn.PassSkirmishActions();

		// Shelob is fierce: the fierce round is passed over, and the regroup phase begins.
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(3, scn.GetStrength(bilbo));
	}

	@Test
	public void SkirmishActionMakesBilboStrength2MoreWhileHeCanSpotGollum() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var bifur = scn.GetFreepsCard("bifur");
		var gollum = scn.GetShadowCard("gollum");
		scn.MoveCardsToSupportArea(bifur);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bifur);
		scn.MoveMinionsToTable(gollum);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, gollum);
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(3, scn.GetStrength(bilbo));
		scn.FreepsUseCardAction(bilbo);
		assertInZone(Zone.DISCARD, bifur);
		assertEquals(5, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishActionMakesBilboStrength2MoreWhileGollumHalfAWraithWaitsInAnotherSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var bifur = scn.GetFreepsCard("bifur");
		var gollum = scn.GetShadowCard("wraithgollum");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(bifur);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, bifur);
		scn.MoveMinionsToTable(gollum, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{bilbo, runner}, new PhysicalCardImpl[]{balin, gollum});
		scn.FreepsResolveSkirmish(bilbo);

		assertEquals(3, scn.GetStrength(bilbo));
		scn.FreepsUseCardAction(bilbo);
		assertInZone(Zone.DISCARD, bifur);
		assertEquals(5, scn.GetStrength(bilbo));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void NeitherActionIsOfferedWithoutADwarvenFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bilbo = scn.GetRingBearer();
		var balin = scn.GetFreepsCard("balin");
		var gwaihir = scn.GetFreepsCard("gwaihir");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToSupportArea(gwaihir);

		scn.StartGame();
		assertInZone(Zone.SUPPORT, gwaihir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bilbo));

		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bilbo, runner);
		scn.FreepsResolveSkirmish(bilbo);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(bilbo));
	}
}
