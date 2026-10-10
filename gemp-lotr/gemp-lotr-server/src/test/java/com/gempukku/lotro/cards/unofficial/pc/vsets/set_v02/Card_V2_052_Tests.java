package com.gempukku.lotro.cards.unofficial.pc.vsets.set_v02;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_V2_052_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ruin", "102_52");

					// Valiant companions of two cultures.
					put("guard1", "5_83");       // Household Guard: [rohan] Man, strength 6, vitality 3; "Valiant" is his whole text
					put("guard2", "5_83");
					put("haldir", "102_8");      // Haldir, Naith Commander: [elven] Elf, strength 6; his text needs a ranged weapon or an [elven] possession
					// Rider of Rohan: a [rohan] companion who is not valiant, strength 5, vitality 3.  His ability needs a mount.
					put("rider", "4_286");
					// Deor: a [rohan] ally, a Man.  No ally is valiant in print; in one test the Flame makes him valiant.
					put("deor", "7_222");
					// Northern Signal-fire, Flame of Calenhad: a [rohan] possession in the support area.  "Maneuver:
					// Hinder this beacon and exert an unbound Man.  Until the regroup phase, each of your other Men
					// gain each of the unloaded keywords (except race) on that Man."
					put("flame", "103_86");
					// Eowyn, Restless Warrior: "Each time the fellowship moves during the regroup phase, you may
					// reinforce a [rohan] token".  Not valiant.
					put("eowyn", "13_124");

					// [rohan] conditions: Arrow-slits is unique, Doom Drove Them is not.  Both cost 1 and play
					// to the support area with nothing to spot.
					put("slits1", "5_80");
					put("slits2", "5_80");
					put("doom", "8_86");
					// [rohan] events.
					put("villagers", "4_273");   // Fight for the Villagers: Skirmish, cost 1. "Make a [rohan] Man strength +2 (or +3 if you spot a villager)."
					put("work", "4_297");        // Work for the Sword: another skirmish event, kept in hand
					put("knights", "7_238");     // Knights of His House: Maneuver, cost 0, nothing to spot
					put("precise", "18_101");    // Precise Attack: Archery; "Discard a [rohan] possession from play to choose one: ..."
					// [rohan] cards that are neither conditions nor events.
					put("chain", "4_278");       // Heavy Chain: a possession, bearer must be a Man
					put("bow", "5_88");          // Rohirrim Bow: "Archery: Exert bearer and discard this possession to wound a minion."
					// A condition and a skirmish event of another culture, both playable with Haldir in play.
					put("arms", "4_82");         // Strength of Arms: [elven] condition, support area, cost 1
					put("defiance", "1_37");     // Defiance: "Skirmish: Make an Elf strength +2 ..."
					// Stand Against Darkness: "Maneuver: Exert an Elf to discard a [sauron] minion ..."
					put("stand", "1_63");

					put("runner1", "1_178");     // Goblin Runner: [moria], strength 5, vitality 1, cost 1, site 4
					put("runner2", "1_178");
					put("savage", "1_151");      // Uruk Savage: [isengard], strength 5, vitality 3, cost 2, site 5
					put("troop", "1_177");       // Goblin Patrol Troop: strength 13, vitality 3, cost 6, site 4, no text
					put("hunter", "1_256");      // Morgul Hunter: a [sauron] minion, stronger for each companion
					put("cantea", "1_230");      // Ulaire Cantea: a minion kept in the Shadow player's hand
					// Shadow cards that are not minions, for the Shadow player's discard pile.
					put("sword", "1_160");       // Uruk-hai Sword: a possession
					put("child", "4_148");       // Down to the Last Child: a condition
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NowforRuinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: V2
		 * Name: Now for Ruin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a minion is killed, spot 2 valiant companions to add a [rohan] token here.
		 * Each time there are 3 [rohan] tokens here, remove all tokens here to play a [rohan] condition or event
		 * from your discard pile. The Shadow player may then play a minion from their discard pile.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ruin");

		assertEquals("Now for Ruin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedForOneToTheSupportArea() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		scn.MoveCardsToHand(ruin);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.FreepsPlayAvailable(ruin));

		scn.FreepsPlayCard(ruin);
		assertEquals(1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, ruin);
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachMinionKilledAddsATokenWhileTwoValiantCompanionsCanBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var rider = scn.GetFreepsCard("rider");
		var bow = scn.GetFreepsCard("bow");
		var slits = scn.GetFreepsCard("slits1");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(ruin);
		// Exactly two valiant companions, a [rohan] Man and an [elven] Elf, and one who is not valiant.
		scn.MoveCompanionsToTable(guard, haldir, rider);
		scn.AttachCardsTo(guard, bow);
		// Something the card could play, were there 3 tokens.
		scn.MoveCardsToDiscard(slits);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		// Vitality 3 with 2 wounds: losing a skirmish kills the Uruk Savage.
		scn.AddWoundsToChar(savage, 2);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetCultureTokensOn(ruin));

		// A [moria] minion killed by a wound from a card, in the archery phase.
		scn.FreepsUseCardAction(bow);
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetCultureTokensOn(ruin));

		// An [isengard] minion killed by losing a skirmish.
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(haldir, savage);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, savage);
		assertEquals(2, scn.GetCultureTokensOn(ruin));

		// Two tokens are not three: nothing is played.
		assertInZone(Zone.DISCARD, slits);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TwoMinionsKilledInOneSkirmishAddTwoTokens() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(ruin);
		scn.MoveCompanionsToTable(guard1, guard2);
		// 6 + 5 = 11, to beat two Goblin Runners at once (5 + 5).
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard1, null, 5));

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		// One Runner assigned by each player to the same companion.
		scn.FreepsAssignToMinions(guard1, runner1);
		scn.ShadowAssignToMinions(guard1, runner2);
		scn.FreepsResolveSkirmish(guard1);

		assertEquals(11, scn.GetStrength(guard1));
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner1, runner2);
		assertEquals(2, scn.GetCultureTokensOn(ruin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoTokenIsAddedWithOnlyOneValiantCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var deor = scn.GetFreepsCard("deor");
		var flame = scn.GetFreepsCard("flame");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(ruin, flame);
		// One valiant companion.  Beside him Frodo, a companion who is not valiant, and an ally.
		scn.MoveCompanionsToTable(guard);
		scn.MoveAlliesToTable(deor);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		// No ally is valiant in print.  In the maneuver phase the Flame of Calenhad has the Household Guard,
		// the only unbound Man, exert, and until the regroup phase Deor, the player's other Man, has his
		// keyword: a second valiant character, and no companion.
		scn.SkipToPhase(Phase.MANEUVER);
		assertFalse(scn.HasKeyword(deor, Keyword.VALIANT));
		scn.FreepsUseCardAction(flame);
		assertEquals(1, scn.GetWoundsOn(guard));

		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, runner);

		assertTrue(scn.HasKeyword(guard, Keyword.VALIANT));
		assertTrue(scn.HasKeyword(deor, Keyword.VALIANT));
		assertFalse(scn.HasKeyword(frodo, Keyword.VALIANT));
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NoTokenIsAddedWhenAMinionIsDiscardedNorWhenACompanionIsKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var rider = scn.GetFreepsCard("rider");
		var stand = scn.GetFreepsCard("stand");
		var hunter = scn.GetShadowCard("hunter");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner1");
		scn.MoveCardsToSupportArea(ruin);
		scn.MoveCompanionsToTable(guard, haldir, rider);
		scn.MoveCardsToHand(stand);

		scn.StartGame();
		scn.MoveMinionsToTable(hunter, troop, runner);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetCultureTokensOn(ruin));

		// Stand Against Darkness discards the [sauron] minion: discarded, not killed.
		scn.FreepsPlayCard(stand);
		assertInZone(Zone.DISCARD, hunter);
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetCultureTokensOn(ruin));

		// The Rider of Rohan, strength 5, is overwhelmed by the Goblin Patrol Troop, strength 13: a
		// companion killed, with both valiant companions still there.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { rider, troop },
				new PhysicalCardImpl[] { guard, runner });
		scn.FreepsResolveSkirmish(rider);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, rider);
		assertEquals(0, scn.GetCultureTokensOn(ruin));

		// On the same board a minion killed does add one.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(1, scn.GetCultureTokensOn(ruin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtThreeTokensAllAreRemovedToPlayARohanEventFromTheDiscardPileAndTheShadowPlayerMayThenPlayAMinionFromHis() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var slits1 = scn.GetFreepsCard("slits1");
		var slits2 = scn.GetFreepsCard("slits2");
		var doom = scn.GetFreepsCard("doom");
		var villagers = scn.GetFreepsCard("villagers");
		var work = scn.GetFreepsCard("work");
		var knights = scn.GetFreepsCard("knights");
		var chain = scn.GetFreepsCard("chain");
		var rider = scn.GetFreepsCard("rider");
		var arms = scn.GetFreepsCard("arms");
		var defiance = scn.GetFreepsCard("defiance");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		var cantea = scn.GetShadowCard("cantea");
		var sword = scn.GetShadowCard("sword");
		var child = scn.GetShadowCard("child");
		scn.MoveCardsToSupportArea(ruin, slits1);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCompanionsToTable(guard, haldir);
		// In the discard pile.  [rohan] and a condition or an event that can be played now: Doom Drove
		// Them and Fight for the Villagers.  Not so: Arrow-slits (unique, and its other copy is in
		// play), Knights of His House (a maneuver event, and this is a skirmish), Heavy Chain (a
		// possession), a Rider of Rohan (a companion), Strength of Arms (an [elven] condition) and
		// Defiance (an [elven] skirmish event).  A second [rohan] skirmish event is in hand.
		scn.MoveCardsToDiscard(slits2, doom, villagers, knights, chain, rider, arms, defiance);
		scn.MoveCardsToHand(work);
		// The Shadow player's discard pile: two minions, a possession and a condition.  A third minion
		// is in his hand.
		scn.MoveCardsToDiscard(savage, troop, sword, child);
		scn.MoveCardsToHand(cantea);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, runner);

		assertEquals(2, scn.GetCultureTokensOn(ruin));
		// 3 companions + 2 for the site.
		assertEquals(5, scn.GetTwilight());
		assertEquals(6, scn.GetStrength(guard));

		// The Guard (6) kills the Goblin Runner (5): the third token.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetCultureTokensOn(ruin));

		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom, villagers));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(slits2, knights, chain, rider, arms, defiance, work));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCardBPFromSelection(villagers);

		// The event is carried out (the Guard is the only [rohan] Man) and goes back to the discard pile.
		assertEquals(8, scn.GetStrength(guard));
		assertInZone(Zone.DISCARD, villagers);
		// 5 + 1 for the event.
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.DISCARD, doom);
		assertInZone(Zone.HAND, work);

		// The Shadow player: the Uruk Savage and the Goblin Runner just killed are offered.  The Goblin
		// Patrol Troop costs 6 + 2 roaming, more than the pool holds; the others are not minions or
		// not in the discard pile.
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, sword, child, cantea));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCardBPFromSelection(savage);

		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		// 6 - 2 for the Uruk Savage - 2 roaming (its site is 5, the fellowship is at 2).
		assertEquals(2, scn.GetTwilight());
		assertInZone(Zone.DISCARD, runner, troop);
		assertInZone(Zone.HAND, cantea);
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ARohanConditionChosenGoesToTheSupportAreaAndTheShadowPlayerMayDecline() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var doom = scn.GetFreepsCard("doom");
		var villagers = scn.GetFreepsCard("villagers");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCardsToSupportArea(ruin);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCompanionsToTable(guard, haldir);
		scn.MoveCardsToDiscard(doom, villagers);
		scn.MoveCardsToDiscard(savage);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, runner);
		scn.PassSkirmishActions();

		assertTrue(scn.FreepsHasCardChoiceAvailable(doom, villagers));
		// 3 companions + 2 for the site.
		assertEquals(5, scn.GetTwilight());
		scn.FreepsChooseCardBPFromSelection(doom);

		assertInZone(Zone.SUPPORT, doom);
		assertEquals(6, scn.GetTwilight());
		assertInZone(Zone.DISCARD, villagers);
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertEquals(0, scn.GetCultureTokensOn(doom));

		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, runner));
		scn.ShadowDeclineChoosing();

		assertInZone(Zone.DISCARD, savage, runner);
		assertEquals(6, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void PlaysAgainEachTimeTheTokensReachThree() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var villagers = scn.GetFreepsCard("villagers");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(ruin);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCompanionsToTable(guard, haldir);
		// The one card that can be played: an event, which is back in the discard pile afterwards.
		scn.MoveCardsToDiscard(villagers);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, runner1 },
				new PhysicalCardImpl[] { haldir, runner2 });

		scn.FreepsResolveSkirmish(guard);
		assertEquals(5, scn.GetTwilight());
		scn.PassSkirmishActions();
		// Fight for the Villagers is played: the only card to choose, on the only [rohan] Man.
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertEquals(8, scn.GetStrength(guard));
		assertInZone(Zone.DISCARD, villagers);
		assertEquals(6, scn.GetTwilight());
		scn.ShadowDeclineChoosing();

		// Two tokens again, and a second minion killed in the next skirmish.
		scn.AddTokensToCard(ruin, 2);
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(2, scn.GetCultureTokensOn(ruin));
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner2);
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.DISCARD, villagers);
		assertEquals(7, scn.GetTwilight());
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		scn.ShadowDeclineChoosing();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheThirdTokenMayComeFromAReinforcement() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var eowyn = scn.GetFreepsCard("eowyn");
		var slits = scn.GetFreepsCard("slits1");
		var ownSavage = scn.GetFreepsCard("savage");
		scn.MoveCardsToSupportArea(ruin);
		scn.AddTokensToCard(ruin, 2);
		// No valiant companion and no minion: nothing is killed in this test.
		scn.MoveCompanionsToTable(eowyn);
		// The Free Peoples player's own copy of a minion is in his discard pile; the Shadow player's
		// discard pile is empty.
		scn.MoveCardsToDiscard(slits, ownSavage);

		scn.StartGame();
		scn.SkipToMovementDecision();

		assertEquals(2, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.DISCARD, slits);

		// The fellowship moves in the regroup phase and Eowyn reinforces the only [rohan] token-bearer.
		scn.FreepsChooseToMove();
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsAcceptOptionalTrigger();

		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.SUPPORT, slits);
		// "Their discard pile": the Shadow player has no minion in his own, and is not asked.
		assertEquals(0, scn.GetShadowDiscardCount());
		assertInZone(Zone.DISCARD, ownSavage);
		assertFalse(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ATokenAddedToAnotherCardSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var doom = scn.GetFreepsCard("doom");
		var eowyn = scn.GetFreepsCard("eowyn");
		var slits = scn.GetFreepsCard("slits1");
		scn.MoveCardsToSupportArea(ruin, doom);
		// Three tokens here that found nothing to play when the third came, and a card that has
		// reached the discard pile since.  Another [rohan] condition has a token of its own.
		scn.AddTokensToCard(ruin, 3);
		scn.AddTokensToCard(doom, 1);
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToDiscard(slits);

		scn.StartGame();
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();

		// Eowyn's reinforcement goes to the other condition.
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(eowyn));
		scn.FreepsAcceptOptionalTrigger();
		assertTrue(scn.FreepsHasCardChoiceAvailable(ruin, doom));
		scn.FreepsChooseCard(doom);

		assertEquals(2, scn.GetCultureTokensOn(doom));
		assertEquals(3, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.DISCARD, slits);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithNothingToPlayTheTokensStayAndTheShadowPlayerIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var chain = scn.GetFreepsCard("chain");
		var arms = scn.GetFreepsCard("arms");
		var runner = scn.GetShadowCard("runner1");
		var savage = scn.GetShadowCard("savage");
		var theirDoom = scn.GetShadowCard("doom");
		scn.MoveCardsToSupportArea(ruin);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCompanionsToTable(guard, haldir);
		// A [rohan] possession and an [elven] condition: nothing this card could play.
		scn.MoveCardsToDiscard(chain, arms);
		// The other player's discard pile: a minion, and his own copy of a [rohan] condition.
		scn.MoveCardsToDiscard(savage, theirDoom);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, runner);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		// The third token is added, and that is all.
		assertEquals(3, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.DISCARD, chain, arms, savage, theirDoom);
		assertFalse(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFourthTokenPlaysACardThatHasReachedTheDiscardPileSinceTheThird() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var doom = scn.GetFreepsCard("doom");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCardsToSupportArea(ruin);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCompanionsToTable(guard, haldir);

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { guard, runner1 },
				new PhysicalCardImpl[] { haldir, runner2 });

		// The third token, with an empty discard pile.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(3, scn.GetCultureTokensOn(ruin));

		// A [rohan] condition reaches the discard pile; that alone sets nothing off.
		scn.MoveCardsToDiscard(doom);
		scn.FreepsResolveSkirmish(haldir);
		assertEquals(3, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.DISCARD, doom);

		// The fourth token: all four are removed and the condition is played.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner2);
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.SUPPORT, doom);
		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		scn.ShadowDeclineChoosing();
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheShadowPlayerIsNotAskedWhenHeCanPayForNoMinionInHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var doom = scn.GetFreepsCard("doom");
		var runner = scn.GetShadowCard("runner1");
		var child = scn.GetShadowCard("child");
		scn.MoveCardsToSupportArea(ruin);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCompanionsToTable(guard, haldir);
		scn.MoveCardsToDiscard(doom);
		scn.MoveCardsToDiscard(child);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignAndResolve(guard, runner);
		// An empty pool.  Doom Drove Them will add 1; the Goblin Runner about to die costs 1 + 2 roaming.
		scn.SetTwilight(0);
		scn.PassSkirmishActions();

		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetCultureTokensOn(ruin));
		assertInZone(Zone.SUPPORT, doom);
		assertEquals(1, scn.GetTwilight());
		assertFalse(scn.ShadowDecisionAvailable("Choose card from discard"));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void InTheArcheryPhaseARohanArcheryEventIsOfferedAndASkirmishEventIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ruin = scn.GetFreepsCard("ruin");
		var guard = scn.GetFreepsCard("guard1");
		var haldir = scn.GetFreepsCard("haldir");
		var bow = scn.GetFreepsCard("bow");
		var chain = scn.GetFreepsCard("chain");
		var precise = scn.GetFreepsCard("precise");
		var villagers = scn.GetFreepsCard("villagers");
		var doom = scn.GetFreepsCard("doom");
		var runner = scn.GetShadowCard("runner1");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCardsToSupportArea(ruin);
		scn.AddTokensToCard(ruin, 2);
		scn.MoveCompanionsToTable(guard, haldir);
		// The Bow kills; the Chain is the [rohan] possession Precise Attack would need to discard.
		scn.AttachCardsTo(guard, bow, chain);
		scn.MoveCardsToDiscard(precise, villagers, doom);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, troop);
		scn.SkipToPhase(Phase.ARCHERY);

		scn.FreepsUseCardAction(bow);
		scn.FreepsChooseCard(runner);
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetCultureTokensOn(ruin));

		assertTrue(scn.FreepsDecisionAvailable("Choose card from discard"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(precise, doom));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(villagers));
		scn.FreepsChooseCardBPFromSelection(doom);
		assertInZone(Zone.SUPPORT, doom);

		assertTrue(scn.ShadowDecisionAvailable("Choose card from discard"));
		scn.ShadowDeclineChoosing();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
	}
}
