package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_209_Tests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("late", "7_209");
					put("late2", "7_209");
					put("nelya", "2_84");       // Úlairë Nelya, Ringwraith in Twilight: a [wraith] Nazgûl, strength 10; nothing below 2 burdens or 2 wounds on the Ring-bearer
					put("khamul", "103_61");    // Ulaire Attea, Khamul of the East: a [raider] Nazgûl, strength 12, fierce; nothing below 3 burdens
					put("creeper", "8_75");     // Morgul Creeper: a [wraith] minion who is an Orc and no Nazgûl, strength 8; nothing below 3 burdens
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");     // Boromir, Lord of Gondor: strength 7, vitality 3, not overwhelmed unless his strength is tripled
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: strength 6, +3 while skirmishing a Nazgûl
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally, strength 8, home site 3; "Fellowship: Exert Elrond to draw a card."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetExertScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("late", "7_209");
					put("late2", "7_209");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: vitality 4
					put("sam", "1_311");        // Sam, Son of Hamfast: vitality 4
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally; "Fellowship: Exert Elrond to draw a card."
					// Dear Friends: [shire] Fellowship event, cost 0, "Exert a companion to place a Free Peoples card from
					// your discard pile beneath your draw deck": an exertion of the player's choice.
					put("friends1", "2_98");
					put("friends2", "2_98");
					put("friends3", "2_98");
					// Mûmak Commander, Giant Among the Swertings: "Maneuver: Exert Mûmak Commander twice to exert a
					// companion twice (except the Ring-bearer)."
					put("commander", "15_86");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TooLateStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Too Late
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Skirmish:</b> Transfer this condition from your support area to a character skirmishing a Nazgûl.<br>Each time bearer exerts, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("late");

		assertEquals("Too Late", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void InASkirmishAgainstANazgulACopyInYourSupportAreaIsTransferredToTheCompanionFightingIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetShadowCard("late");
		var late2 = scn.GetShadowCard("late2");
		var nelya = scn.GetShadowCard("nelya");
		var khamul = scn.GetShadowCard("khamul");
		var creeper = scn.GetShadowCard("creeper");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var arwen = scn.GetFreepsCard("arwen");
		scn.MoveCompanionsToTable(aragorn, boromir, arwen);

		scn.StartGame();
		scn.MoveCardsToSupportArea(late, late2);
		assertInZone(Zone.SUPPORT, late, late2);
		scn.MoveMinionsToTable(nelya, khamul, creeper);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, nelya}, new PhysicalCardImpl[]{boromir, creeper},
				new PhysicalCardImpl[]{arwen, khamul});

		// First, Boromir against the Morgul Creeper, a [wraith] minion who is no Nazgûl.  Aragorn and Arwen are
		// assigned to Nazgûl, but their skirmishes have not begun: nobody is skirmishing a Nazgûl.
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowActionAvailable(late));
		assertFalse(scn.ShadowActionAvailable(late2));
		scn.ShadowPass();

		// 8 against 7: Boromir loses.
		assertEquals(1, scn.GetWoundsOn(boromir));

		// Now Aragorn against Nelya: each copy offers itself.
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();
		assertTrue(scn.ShadowActionAvailable(late));
		assertTrue(scn.ShadowActionAvailable(late2));

		scn.ShadowUseCardAction(late);

		// Aragorn is the one character skirmishing a Nazgûl, so nothing is asked; Arwen, still waiting for Khamûl,
		// is not given it.
		assertAttachedTo(late, aragorn);
		assertNotAttachedTo(late, arwen);
		assertInZone(Zone.SUPPORT, late2);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The copy on Aragorn is no longer in your support area and offers nothing; the other still does.
		scn.FreepsPass();
		assertFalse(scn.ShadowActionAvailable(late));
		assertTrue(scn.ShadowActionAvailable(late2));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void AnAllySkirmishingANazgulIsGivenIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var late = scn.GetShadowCard("late");
		var khamul = scn.GetShadowCard("khamul");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveCardsToSupportArea(late);
		assertInZone(Zone.SUPPORT, late);
		scn.FreepsPass();
		// Site 3: Elrond's home, where he may be assigned.
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.MoveMinionsToTable(khamul);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(elrond, khamul);
		scn.FreepsResolveSkirmish(elrond);
		scn.FreepsPass();
		assertInZone(Zone.SUPPORT, late);
		assertTrue(scn.ShadowActionAvailable(late));

		scn.ShadowUseCardAction(late);

		assertAttachedTo(late, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void EachTimeItsBearerExertsOneTwilightIsAdded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertScenario();

		var late = scn.GetShadowCard("late");
		var late2 = scn.GetShadowCard("late2");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var friends1 = scn.GetFreepsCard("friends1");
		var friends2 = scn.GetFreepsCard("friends2");
		var friends3 = scn.GetFreepsCard("friends3");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(friends1, friends2, friends3);
		assertInHand(friends1, friends2, friends3);

		scn.StartGame();
		// One copy on a companion, one on an ally.
		scn.AttachCardsTo(aragorn, late);
		scn.AttachCardsTo(elrond, late2);
		scn.FreepsRefreshDecision();
		assertAttachedTo(late, aragorn);
		assertAttachedTo(late2, elrond);
		assertEquals(0, scn.GetTwilight());

		// Sam, who bears neither copy, exerts: nothing is added.
		scn.FreepsPlayCard(friends1);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, sam));
		scn.FreepsChooseCard(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetTwilight());

		// Aragorn exerts: his copy adds 1, Elrond's nothing.
		scn.FreepsPlayCard(friends2);
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetTwilight());

		// Elrond exerts by his own ability: his copy adds 1.
		assertTrue(scn.FreepsActionAvailable(elrond));
		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(2, scn.GetTwilight());

		// Aragorn exerts again: 1 more.
		scn.FreepsPlayCard(friends3);
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void OneEffectThatExertsTheBearerTwiceAddsTwo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetExertScenario();

		var late = scn.GetShadowCard("late");
		var commander = scn.GetShadowCard("commander");
		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, late);
		assertAttachedTo(late, aragorn);
		scn.MoveMinionsToTable(commander);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int pool = scn.GetTwilight();
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(commander));
		assertTrue(scn.ShadowActionAvailable(commander));
		scn.ShadowUseCardAction(commander);
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, sam));
		scn.ShadowChooseCard(aragorn);

		// The Commander's own two exertions add nothing; Aragorn's two add 1 each.
		assertEquals(2, scn.GetWoundsOn(commander));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(pool + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
