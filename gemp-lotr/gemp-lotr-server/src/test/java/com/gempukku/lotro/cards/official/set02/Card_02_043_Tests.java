package com.gempukku.lotro.cards.official.set02;

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

public class Card_02_043_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "2_43");
					put("lurtz", "1_127");      // Lurtz, Servant of Isengard: unique [isengard] Uruk-hai, strength 13, vitality 3, archer, damage +1; Maneuver: spot another Uruk-hai to make him fierce
					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, strength 9, vitality 3, damage +1 and nothing else

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: companion, strength 7, vitality 3; not overwhelmed unless his strength is tripled
					put("gimli", "1_13");       // Gimli, Son of Gloin: companion, strength 6, vitality 3, damage +1; a skirmish ability used only on purpose
					put("sam", "1_311");        // Sam, Son of Hamfast: a card for the Free Peoples draw deck
					put("merry", "1_302");      // Merry, Friend to Sam: a card for the Free Peoples draw deck
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetUrukHaiLurtzScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("sword", "2_43");
					put("lurtz", "11_194");     // Lurtz, Minion of the White Wizard: unique [uruk-hai] Uruk-hai, archer, damage +1, muster; strength +3 for each exhausted companion
					put("raiding", "1_158");    // Uruk-hai Raiding Party: [isengard] Uruk-hai, damage +1 and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion, strength 8, vitality 4; a maneuver ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LurtzsSwordStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Lurtz's Sword
		 * Unique: True
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: +2
		 * Game Text: Bearer must be Lurtz.<br>He is <b>damage +1</b>.<br>Each time Lurtz wins a skirmish, the Free Peoples player must discard the top 2 cards of his or her draw deck.
		*/

		var scn = GetScenario();

		var sword = scn.GetFreepsCard("sword");

		assertEquals("Lurtz's Sword", sword.getBlueprint().getTitle());
		assertNull(sword.getBlueprint().getSubtitle());
		assertTrue(sword.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, sword.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, sword.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, sword.getBlueprint().getCardType());
		assertTrue(sword.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, sword.getBlueprint().getTwilightCost());
		assertEquals(2, sword.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnLurtzAloneAndMakesHimDamagePlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		var raiding = scn.GetShadowCard("raiding");
		scn.MoveCardsToHand(sword, lurtz);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding);
		scn.SetTwilight(20);
		scn.FreepsPass();

		// An Uruk-hai who is not Lurtz cannot bear it.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(sword));

		scn.ShadowPlayCard(lurtz);
		assertTrue(scn.ShadowPlayAvailable(sword));
		// Each is printed damage +1.
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiding, Keyword.DAMAGE));

		// Lurtz is the one bearer it can go on, so nothing is asked.
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, lurtz);
		assertEquals(2, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(raiding, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnUrukHaiCultureLurtzBearsItAndIsDamagePlus1Too() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukHaiLurtzScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		scn.MoveCardsToHand(sword);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// This Lurtz is [uruk-hai], not [isengard], and the text names only the title.  He is printed damage +1.
		assertEquals(Culture.URUK_HAI, lurtz.getBlueprint().getCulture());
		assertEquals(1, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertTrue(scn.ShadowPlayAvailable(sword));
		scn.ShadowPlayCard(sword);
		assertAttachedTo(sword, lurtz);
		assertEquals(2, scn.GetKeywordCount(lurtz, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void LurtzWinningASkirmishTheFreePeoplesPlayerDiscardsTheTopTwoCardsOfHisDrawDeck() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		var raiding = scn.GetShadowCard("raiding");
		var aragorn = scn.GetFreepsCard("aragorn");
		var top1 = scn.GetFreepsCard("sam");
		var top2 = scn.GetFreepsCard("merry");
		var top3 = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveCardsToTopOfDeck(top3, top2, top1);
		assertEquals(top1, scn.GetFreepsDrawDeck().get(0));
		assertEquals(top2, scn.GetFreepsDrawDeck().get(1));
		assertEquals(top3, scn.GetFreepsDrawDeck().get(2));
		assertInZone(Zone.DECK, top1, top2, top3);
		scn.MoveMinionsToTable(raiding);
		// Lurtz is an archer: he goes down after the archery phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();
		int shadowDeck = scn.GetShadowDeckCount();
		int shadowDiscard = scn.GetShadowDiscardCount();

		// Lurtz, 13 + 2, beats Aragorn, 8: 3 wounds with damage +2, and the top two cards go, with nothing asked.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.DISCARD, top1, top2);
		assertEquals(top3, scn.GetFreepsTopOfDeck());
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());
		// Not the Shadow player's deck.
		assertEquals(shadowDeck, scn.GetShadowDeckCount());
		assertEquals(shadowDiscard, scn.GetShadowDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LurtzLosingASkirmishOrAnotherUrukhaiWinningOneDiscardsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		var raiding = scn.GetShadowCard("raiding");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		scn.StartGame();
		// Aragorn at 18 beats Lurtz at 15, who lives through the one wound.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));
		scn.MoveMinionsToTable(raiding);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, lurtz}, new PhysicalCardImpl[]{gimli, raiding});
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		// The Raiding Party, 9, beats Gimli, 6, and does not bear the sword: nothing discarded.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// Lurtz loses: nothing discarded.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassCurrentPhaseActions();
		assertEquals(1, scn.GetWoundsOn(lurtz));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(deck, scn.GetFreepsDeckCount());
		assertEquals(discard, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void EachTimeLurtzWinsTwoMoreCardsAreDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		var raiding = scn.GetShadowCard("raiding");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.MoveMinionsToTable(lurtz, raiding);
		scn.AttachCardsTo(lurtz, sword);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Lurtz makes himself fierce, spotting the Raiding Party.
		scn.ShadowUseCardAction(lurtz);
		assertTrue(scn.HasKeyword(lurtz, Keyword.FIERCE));
		scn.SkipToArcheryWounds();

		// Lurtz is an archer: the Free Peoples player places the one archery wound, on Frodo.
		scn.FreepsChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));

		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.ShadowAssignToMinions(boromir, raiding);
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		// Lurtz, 15, beats Aragorn, 8: two cards.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());

		// The Raiding Party, 9, beats Boromir, 7: Boromir lives through 2 wounds, and nothing more is discarded.
		scn.FreepsResolveSkirmish(boromir);
		scn.PassCurrentPhaseActions();
		assertEquals(2, scn.GetWoundsOn(boromir));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());

		// And Lurtz wins again in the fierce skirmish, killing Boromir: two more.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, lurtz);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertInZone(Zone.DEAD, boromir);
		assertEquals(deck - 4, scn.GetFreepsDeckCount());
		assertEquals(discard + 4, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AnUrukHaiCultureLurtzWinningASkirmishMakesTheFreePeoplesPlayerDiscardTwoCardsToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetUrukHaiLurtzScenario();

		var sword = scn.GetShadowCard("sword");
		var lurtz = scn.GetShadowCard("lurtz");
		var raiding = scn.GetShadowCard("raiding");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.MoveMinionsToTable(raiding);
		// This Lurtz is an archer too: he goes down after the archery phase.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.MoveMinionsToTable(lurtz);
		scn.AttachCardsTo(lurtz, sword);
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, lurtz);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		int deck = scn.GetFreepsDeckCount();
		int discard = scn.GetFreepsDiscardCount();

		// Lurtz, 13 + 2 (no companion exhausted yet), beats Aragorn, 8: two cards.
		assertEquals(Culture.URUK_HAI, lurtz.getBlueprint().getCulture());
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(deck - 2, scn.GetFreepsDeckCount());
		assertEquals(discard + 2, scn.GetFreepsDiscardCount());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
