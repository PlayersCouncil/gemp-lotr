package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Keyword;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_118_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valiant", "1_118");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor], strength 8, vitality 4
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion who is no Aragorn, strength 7, vitality 3
					put("savage1", "1_151");    // Uruk Savage: strength 5, vitality 3, damage +1 and nothing else
					put("savage2", "1_151");
					put("raiders", "1_158");    // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetSkirmishScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valiant", "1_118");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4

					// Lurtz, Servant of Isengard: archer, damage +1, strength 13 (under twice Aragorn's 8); his
					// maneuver ability needs another Uruk-hai
					put("lurtz", "1_127");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetFierceScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valiant", "1_118");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4
					// Three companions more, so that with Frodo and Aragorn the Uruk Fighter can spot 5
					put("gimli", "1_13");       // Gimli, Son of Gloin: his skirmish ability is used only on purpose
					put("merry", "1_303");      // Merry, From O'er the Brandywine: strength +2 while he bears a weapon
					put("pippin", "1_306");     // Pippin, Friend to Frodo: guards [shire] tales
					// Uruk Fighter: strength 8 (a tie goes to the Shadow side), damage +1, and "Maneuver: Spot 5
					// companions to make this minion fierce until the regroup phase"
					put("fighter", "1_146");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetRohanScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valiant", "1_118");
					// Aragorn, Defender of Rohan: the one Aragorn who is not [gondor]; his hunter text needs the
					// Ring-bearer assigned
					put("aragorn", "17_93");
					put("savage", "1_151");     // Uruk Savage: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetNoAragornScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("valiant", "1_118");
					put("boromir", "1_97");     // Boromir, Son of Denethor: a [gondor] companion who is no Aragorn
					put("savage", "1_151");     // Uruk Savage: a minion, so that there is a maneuver phase
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ValiantManoftheWestStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Valiant Man of the West
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Make Aragorn <b>defender +1</b> and he takes no more than 1 wound during each skirmish phase until the regroup phase.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("valiant");

		assertEquals("Valiant Man of the West", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void AragornTakesOneWoundInASkirmishHeLosesToADamageMinionAndAnArcheryWoundIsNoPartOfThat() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetSkirmishScenario();

		var valiant = scn.GetFreepsCard("valiant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(valiant);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.SkipToPhase(Phase.MANEUVER);

		assertFalse(scn.HasKeyword(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(valiant));

		scn.FreepsPlayCard(valiant);
		assertInDiscard(valiant);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// Lurtz's archery wound is no wound of a skirmish phase: it is taken, and it does not use up the one
		// wound the skirmish allows.
		scn.SkipToArcheryWounds();
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 8 against 13: Aragorn loses, and of the two wounds a damage +1 minion deals he takes one.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(lurtz));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AragornIsDefenderPlus1AndTakesOneWoundLosingToTwoDamageMinionsWhileAnotherGondorCompanionTakesBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant = scn.GetFreepsCard("valiant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var savage1 = scn.GetShadowCard("savage1");
		var savage2 = scn.GetShadowCard("savage2");
		var raiders = scn.GetShadowCard("raiders");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveCardsToHand(valiant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1, savage2, raiders);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.FreepsPlayAvailable(valiant));

		scn.FreepsPlayCard(valiant);
		assertInDiscard(valiant);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));

		scn.SkipToAssignments();
		// Defender +1: Aragorn takes both Uruk Savages.
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { aragorn, savage1, savage2 },
				new PhysicalCardImpl[] { boromir, raiders }
		);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// 8 against 5 + 5: Aragorn loses. The wound and the two damage +1 would be 3; he takes no more than 1.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();
		// 7 against 9: Boromir loses and takes the wound and the Raiding Party's damage +1.
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheOneWoundAllowedRenewsInTheFierceSkirmishAndBothEffectsAreGoneFromTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetFierceScenario();

		var valiant = scn.GetFreepsCard("valiant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var fighter = scn.GetShadowCard("fighter");
		scn.MoveCompanionsToTable(aragorn, gimli, merry, pippin);
		scn.MoveCardsToHand(valiant);

		scn.StartGame();
		scn.MoveMinionsToTable(fighter);
		scn.SkipToPhase(Phase.MANEUVER);

		scn.FreepsPlayCard(valiant);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertFalse(scn.HasKeyword(fighter, Keyword.FIERCE));
		scn.ShadowUseCardAction(fighter);
		assertTrue(scn.HasKeyword(fighter, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fighter);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.PassSkirmishActions();
		// 8 against 8, a tie: the Fighter wins. The wound and his damage +1 would be 2; Aragorn takes 1.
		assertEquals(1, scn.GetWoundsOn(aragorn));

		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, fighter);
		scn.FreepsResolveSkirmish(aragorn);
		// A skirmish after one already resolved, and still before the regroup phase: he is still defender +1.
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		scn.PassFierceSkirmishActions();
		// A skirmish phase of its own: 1 wound again (1 in all would leave him at 1, no limit at 4 and dead).
		assertEquals(2, scn.GetWoundsOn(aragorn));

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		// A second move, and a skirmish phase after the regroup phase began: no limit now.
		scn.SkipToMovementDecision();
		scn.FreepsChooseToMove();
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, fighter);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		// The tie goes to the Fighter again: 2 wounds on 2, and vitality 4 is gone (with the limit, 3 and alive).
		assertInZone(Zone.DEAD, aragorn);
		assertFalse(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void MakesTheRohanAragornDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRohanScenario();

		var valiant = scn.GetFreepsCard("valiant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(valiant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(valiant));

		scn.FreepsPlayCard(valiant);
		assertInDiscard(valiant);
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoAragornItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetNoAragornScenario();

		var valiant = scn.GetFreepsCard("valiant");
		var boromir = scn.GetFreepsCard("boromir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir);
		scn.MoveCardsToHand(valiant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.FreepsPlayAvailable(valiant));

		scn.FreepsPlayCard(valiant);
		assertInDiscard(valiant);
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedInTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var valiant = scn.GetFreepsCard("valiant");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage1 = scn.GetShadowCard("savage1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(valiant);

		scn.StartGame();
		scn.MoveMinionsToTable(savage1);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(valiant));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
	}
}
