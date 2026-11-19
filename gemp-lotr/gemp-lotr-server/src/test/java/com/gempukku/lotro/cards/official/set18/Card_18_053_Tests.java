package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_053_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "18_53");
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man, strength 7, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; his own defender ability used only on purpose
					put("merchant", "7_97");      // Gondorian Merchant: the [gondor] Man ally, strength 6, vitality 1; his ability needs initiative
					put("narsil", "9_34");        // Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man; strength only for Elendil or Isildur
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit, the [gondor] companion who is no Man
					put("guard", "5_83");         // Household Guard: a [rohan] Man, the Man who is not [gondor]
					put("gimli", "1_13");         // Gimli, Son of Glóin: a Dwarf, to bear a follower and Dwarven Armor
					put("armor", "1_8");          // Dwarven Armor: a possession in play that is no follower
					// Rohan Stable Master: a [rohan] follower, Aid (1); "Bearer is considered to be mounted."
					put("stablemaster", "15_134");
					put("stablemaster2", "15_134");
					// Pallando, Far-travelling One: a [gandalf] follower, cheated onto Gimli; his Aid declined
					put("pallando", "13_37");
					// Saruman, Servant of Sauron: the Shadow player's follower; his Aid declined
					put("saruman", "17_38");
					put("troop", "1_177");        // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					put("troop2", "1_177");
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5, vitality 1; its play text is not reached by a cheat
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void HornofBoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Horn of Boromir, The Great Horn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype:
		 * Resistance: +1
		 * Game Text: Bearer must be a [gondor] Man.<br><b>Maneuver:</b> Exert bearer and discard a follower from play to make bearer strength +4 until the regroup phase (if bearer is Boromir, he is also <b>defender +1</b> and cannot be overwhelmed unless his strength is tripled).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("Horn of Boromir", card.getBlueprint().getTitle());
		assertEquals("The Great Horn", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getResistance());
	}

	@Test
	public void PlaysOnAGondorManCompanionOrAllyButNotOnAGondorHobbitOrARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var horn = scn.GetFreepsCard("horn");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(boromir, aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(horn);

		scn.StartGame();

		assertInHand(horn);
		assertTrue(scn.FreepsPlayAvailable(horn));
		scn.FreepsPlayCard(horn);

		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(merchant);

		assertAttachedTo(horn, merchant);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ManeuverExertsBearerAndDiscardsAFollowerOfEitherSideFromPlayToMakeBearerStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		var stablemaster2 = scn.GetFreepsCard("stablemaster2");
		var pallando = scn.GetFreepsCard("pallando");
		var saruman = scn.GetShadowCard("saruman");
		scn.MoveCompanionsToTable(aragorn, boromir, gimli);
		scn.AttachCardsTo(aragorn, horn);
		scn.AttachCardsTo(gimli, armor, pallando);
		scn.MoveCardsToSupportArea(stablemaster, saruman);
		scn.MoveCardsToHand(stablemaster2);

		scn.StartGame();
		assertAttachedTo(horn, aragorn);
		assertAttachedTo(pallando, gimli);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, stablemaster, saruman);
		assertInHand(stablemaster2);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		// The followers' Aids are offered as the phase starts; each is declined.
		scn.FreepsDeclineOptionalTrigger();
		scn.ShadowDeclineOptionalTrigger();

		assertAttachedTo(pallando, gimli);
		assertInZone(Zone.SUPPORT, stablemaster, saruman);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.FreepsActionAvailable(horn));
		scn.FreepsUseCardAction(horn);

		// A follower in a support area of either side or borne; no possession, and no follower in hand.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stablemaster, pallando, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(armor, horn, stablemaster2));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pallando);

		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertInDiscard(pallando);
		assertInZone(Zone.SUPPORT, stablemaster, saruman);
		assertInHand(stablemaster2);
		// 8 + 4 on the bearer; Boromir, a [gondor] Man who does not bear it, is left at 7.
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AnAllyBearerExertsToPayAndIsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.MoveAlliesToTable(merchant);
		// Narsil makes his vitality 2, so that he can exert once.
		scn.AttachCardsTo(merchant, narsil, horn);
		scn.MoveCardsToSupportArea(stablemaster);

		scn.StartGame();
		assertAttachedTo(horn, merchant);
		assertInZone(Zone.SUPPORT, stablemaster);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, stablemaster);
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(6, scn.GetStrength(merchant));
		assertTrue(scn.FreepsActionAvailable(horn));
		scn.FreepsUseCardAction(horn);

		// The one follower in play goes unasked.
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertInDiscard(stablemaster);
		assertEquals(6 + 4, scn.GetStrength(merchant));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWhileBearerIsExhaustedThoughAFollowerIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, horn);
		scn.MoveCardsToSupportArea(stablemaster);
		// Vitality 4 with 3 wounds: exhausted.
		scn.AddWoundsToChar(aragorn, 3);

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SUPPORT, stablemaster);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		assertInZone(Zone.SUPPORT, stablemaster);
		assertFalse(scn.FreepsActionAvailable(horn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void NotOfferedWithNoFollowerInPlayThoughOneIsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, horn);
		scn.MoveCardsToHand(stablemaster);

		scn.StartGame();
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertInHand(stablemaster);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertFalse(scn.FreepsActionAvailable(horn));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void TheStrengthBonusLastsUntilTheRegroupPhaseAndANonBoromirBearerGainsNoDefender() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, horn);
		scn.MoveCardsToSupportArea(stablemaster);

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.FreepsUseCardAction(horn);
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, runner}, new PhysicalCardImpl[]{aragorn, savage});
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();

		// In the skirmish after one already resolved: still +4.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(12, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
	}

	@Test
	public void ANonBoromirBearerIsOverwhelmedAtDoubleHisStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var aragorn = scn.GetFreepsCard("aragorn");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, horn);
		scn.MoveCardsToSupportArea(stablemaster);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// A strength that only sets the board: 13 + 11 = 24, double Aragorn's 8 + 4.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 11));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsUseCardAction(horn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(24, scn.GetStrength(troop));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();

		// Vitality 4 with 1 wound: the wound for losing would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, aragorn);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnBoromirItAlsoMakesHimDefenderPlus1AndNotOverwhelmedAtDoubleStrengthUntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var boromir = scn.GetFreepsCard("boromir");
		var narsil = scn.GetFreepsCard("narsil");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		var troop = scn.GetShadowCard("troop");
		var troop2 = scn.GetShadowCard("troop2");
		scn.MoveCompanionsToTable(boromir);
		// Narsil makes his vitality 4, so that the wound for losing the second skirmish does not kill him: only an overwhelm does.
		scn.AttachCardsTo(boromir, horn, narsil);
		scn.MoveCardsToSupportArea(stablemaster);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// A strength that only sets the board: 13 + 9 = 22, double Boromir's 7 + 4 and short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 9));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();

		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(7, scn.GetStrength(boromir));
		scn.FreepsUseCardAction(horn);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(11, scn.GetStrength(boromir));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(22, scn.GetStrength(troop));
		scn.PassSkirmishActions();

		// Not overwhelmed: the one wound for losing, 2 of his 4.
		assertInPlay(boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(7, scn.GetStrength(boromir));

		// A second skirmish phase in the turn, after the regroup phase began: double his 7 overwhelms him again.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.MoveCardsToDiscard(troop);
		scn.MoveMinionsToTable(troop2);
		// 13 + 1 = 14, double 7 and short of triple.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop2, null, 1));
		scn.ShadowRefreshDecision();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop2);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(14, scn.GetStrength(troop2));
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetFreepsDeadCount());
		scn.PassSkirmishActions();

		assertInZone(Zone.DEAD, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnBoromirHeIsOverwhelmedAtExactlyTripleStrength() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var boromir = scn.GetFreepsCard("boromir");
		var stablemaster = scn.GetFreepsCard("stablemaster");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, horn);
		scn.MoveCardsToSupportArea(stablemaster);

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		// A strength that only sets the board: 13 + 20 = 33, exactly triple Boromir's 7 + 4.
		scn.ApplyAdHocModifier(new StrengthModifier(null, troop, null, 20));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsDeclineOptionalTrigger();
		scn.FreepsUseCardAction(horn);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(11, scn.GetStrength(boromir));
		assertEquals(33, scn.GetStrength(troop));
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();

		// Vitality 3 with 1 wound: the wound for losing would not kill him; the overwhelm does.
		assertInZone(Zone.DEAD, boromir);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
