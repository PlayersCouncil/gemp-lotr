package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_136_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("snowmane", "13_136");
					// Éomer, Forthwith Banished: [rohan] Man companion, strength 7, vitality 3; valiant, damage +1 and a
					// cost reduction are his whole text
					put("eomer", "0_66");
					put("guard", "5_83");       // Household Guard: [rohan] Man companion, strength 6, vitality 3; Valiant is its whole text
					// Guma, Plains Farmer: [rohan] Man ally, home 4T; his fellowship ability is used only on purpose
					put("guma", "4_277");
					// Merry, Master Holbytla: a [rohan] Hobbit companion; a Man only while 3 [rohan] companions (or
					// Éowyn) can be spotted, which is never so where he is laid; his texts are used only on purpose
					put("merry", "103_85");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] Man companion; his skirmish ability is not used
					// Doom Drove Them: [rohan] support-area condition that adds [rohan] tokens to itself by its
					// fellowship ability; its skirmish ability is used only on purpose
					put("doom1", "8_86");
					put("doom2", "8_86");
					// Garrison of Osgiliath: [gondor] support-area condition that takes [gondor] tokens as it is played
					put("garrison", "6_52");
					// Sword of Rohan: [rohan] hand weapon on a [rohan] Man; damage +1 only against an Uruk-hai
					put("sword", "4_291");

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1; its only text is on its play
					put("runner2", "1_178");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 and nothing else
					// Théoden, Tall and Proud: [rohan] Man companion, strength 7, vitality 3; valiant, a cost reduction
					// and a trigger on his own death, none of which comes up here
					put("theoden", "8_92");
					// Siege Troop: [sauron] Orc, strength 20, vitality 4 (exhausted by three exertions, not two); a
					// regroup ability used only on purpose
					put("siegetroop", "8_106");
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, strength 11, vitality 3; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SnowmaneStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: Snowmane, Noble Mearas
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: +1
		 * Resistance: +1
		 * Game Text: Bearer must be a [rohan] Man.<br>When you play Snowmane, you may reinforce a [rohan] token.<br><b>Skirmish:</b> If bearer is Théoden, you may remove 2 [rohan] tokens to exhaust a minion he is skirmishing.
		*/

		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");

		assertEquals("Snowmane", snowmane.getBlueprint().getTitle());
		assertEquals("Noble Mearas", snowmane.getBlueprint().getSubtitle());
		assertTrue(snowmane.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, snowmane.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, snowmane.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, snowmane.getBlueprint().getCardType());
		assertTrue(snowmane.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(2, snowmane.getBlueprint().getTwilightCost());
		assertEquals(1, snowmane.getBlueprint().getStrength());
		assertEquals(1, snowmane.getBlueprint().getResistance());
	}

	@Test
	public void PlayedOnARohanManItMayReinforceARohanTokenOnACardThatHasOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var eomer = scn.GetFreepsCard("eomer");
		var guma = scn.GetFreepsCard("guma");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var garrison = scn.GetFreepsCard("garrison");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(eomer, merry, boromir);
		scn.MoveAlliesToTable(guma);
		scn.MoveCardsToSupportArea(doom1, doom2, garrison);
		scn.AddTokensToCard(doom1, 2);
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(snowmane);

		scn.StartGame();

		assertInHand(snowmane);
		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.FreepsPlayCard(snowmane);
		// [rohan] Men: a companion and an ally.  Not Merry (a [rohan] Hobbit, no Man beside two [rohan] companions),
		// Boromir (a [gondor] Man) or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(eomer, guma));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(merry, boromir, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(guma);
		assertAttachedTo(snowmane, guma);

		assertTrue(scn.FreepsHasOptionalTriggerAvailable(snowmane));
		scn.FreepsAcceptOptionalTrigger();
		// Either [rohan] card with a [rohan] token; not Guma (a [rohan] card with none), nor the [gondor] tokens.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, doom2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guma, garrison));
		scn.FreepsChooseCard(doom2);

		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheReinforcementMayBeDeclinedAndAnotherRohanPossessionPlayedAfterIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var eomer = scn.GetFreepsCard("eomer");
		var sword = scn.GetFreepsCard("sword");
		var doom1 = scn.GetFreepsCard("doom1");
		scn.MoveCompanionsToTable(eomer);
		scn.MoveCardsToSupportArea(doom1);
		scn.AddTokensToCard(doom1, 1);
		scn.MoveCardsToHand(snowmane, sword);

		scn.StartGame();

		assertEquals(1, scn.GetCultureTokensOn(doom1));
		// Éomer is the only [rohan] Man: Snowmane goes on him unasked.
		scn.FreepsPlayCard(snowmane);
		assertAttachedTo(snowmane, eomer);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(snowmane));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// Another [rohan] possession played after Snowmane, with the token still there to reinforce.
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, eomer);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(snowmane));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithNoRohanTokenToReinforceTheReinforcementIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var eomer = scn.GetFreepsCard("eomer");
		var doom1 = scn.GetFreepsCard("doom1");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(eomer);
		// A [rohan] card with no token, and [gondor] tokens on a [gondor] card.
		scn.MoveCardsToSupportArea(doom1, garrison);
		scn.AddTokensToCard(garrison, 2);
		scn.MoveCardsToHand(snowmane);

		scn.StartGame();

		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.FreepsPlayCard(snowmane);
		assertAttachedTo(snowmane, eomer);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(snowmane));
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityRemovesTwoRohanTokensToExhaustAMinionTheodenIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var doom1 = scn.GetFreepsCard("doom1");
		var doom2 = scn.GetFreepsCard("doom2");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveCardsToSupportArea(doom1, doom2, garrison);
		scn.AddTokensToCard(doom1, 1);
		scn.AddTokensToCard(doom2, 1);
		scn.AddTokensToCard(garrison, 2);

		var siegetroop = scn.GetShadowCard("siegetroop");
		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();

		assertAttachedTo(snowmane, theoden);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(1, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.MoveMinionsToTable(siegetroop, savage, assassin);
		scn.SkipToAssignments();
		// Two minions on Théoden, the Savage put there by the Shadow player; the Assassin waits for the Guard.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{theoden, siegetroop}, new PhysicalCardImpl[]{guard, assassin});
		scn.ShadowAssignToMinions(theoden, savage);
		scn.FreepsResolveSkirmish(theoden);

		assertEquals(0, scn.GetWoundsOn(siegetroop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(snowmane));

		scn.FreepsUseCardAction(snowmane);
		// One token from each Doom Drove Them, a card at a time; the [gondor] tokens are not offered.
		assertTrue(scn.FreepsHasCardChoiceAvailable(doom1, doom2));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(garrison));
		scn.FreepsChooseCard(doom1);
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetCultureTokensOn(doom2));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		// Either minion Théoden is skirmishing; not the one waiting for the Guard, nor a companion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(siegetroop, savage));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(assassin, theoden, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(siegetroop);

		// Vitality 4: exhausted is three exertions.
		assertEquals(3, scn.GetWoundsOn(siegetroop));
		assertEquals(1, scn.GetVitality(siegetroop));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InAnotherCompanionsSkirmishTheTokensArePaidAndNoMinionIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var doom1 = scn.GetFreepsCard("doom1");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveCardsToSupportArea(doom1);
		scn.AddTokensToCard(doom1, 2);

		var savage = scn.GetShadowCard("savage");
		var assassin = scn.GetShadowCard("assassin");

		scn.StartGame();

		assertAttachedTo(snowmane, theoden);
		assertEquals(2, scn.GetCultureTokensOn(doom1));

		scn.MoveMinionsToTable(savage, assassin);
		scn.SkipToAssignments();
		// The Savage waits for Théoden's skirmish; the Guard's against the Assassin comes first.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{theoden, savage}, new PhysicalCardImpl[]{guard, assassin});
		scn.FreepsResolveSkirmish(guard);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsActionAvailable(snowmane));

		scn.FreepsUseCardAction(snowmane);
		// Both tokens from the one card, unasked; Théoden is skirmishing nobody yet, so nothing is exhausted.
		assertEquals(0, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(assassin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWhileARohanManWhoIsNotTheodenBearsIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var guard = scn.GetFreepsCard("guard");
		var doom1 = scn.GetFreepsCard("doom1");
		scn.MoveCompanionsToTable(theoden, guard);
		scn.AttachCardsTo(guard, snowmane);
		scn.MoveCardsToSupportArea(doom1);
		scn.AddTokensToCard(doom1, 2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(snowmane, guard);
		assertEquals(2, scn.GetCultureTokensOn(doom1));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		// Théoden is in play and two [rohan] tokens are there; the bearer is the Guard.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(snowmane));
		assertEquals(2, scn.GetCultureTokensOn(doom1));
		assertEquals(0, scn.GetWoundsOn(savage));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithOnlyOneRohanToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var snowmane = scn.GetFreepsCard("snowmane");
		var theoden = scn.GetFreepsCard("theoden");
		var doom1 = scn.GetFreepsCard("doom1");
		var garrison = scn.GetFreepsCard("garrison");
		scn.MoveCompanionsToTable(theoden);
		scn.AttachCardsTo(theoden, snowmane);
		scn.MoveCardsToSupportArea(doom1, garrison);
		scn.AddTokensToCard(doom1, 1);
		scn.AddTokensToCard(garrison, 2);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();

		assertAttachedTo(snowmane, theoden);
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));

		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(theoden, savage);
		scn.FreepsResolveSkirmish(theoden);

		// One [rohan] token; the [gondor] tokens do not make up the second.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(snowmane));
		assertEquals(1, scn.GetCultureTokensOn(doom1));
		assertEquals(2, scn.GetCultureTokensOn(garrison));
		assertEquals(0, scn.GetWoundsOn(savage));
	}
}
