package com.gempukku.lotro.cards.official.set11;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_11_112_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halls", "11_112");

					put("miscreant", "11_131");  // Orc Miscreant: [orc] Orc, strength 7, vitality 3; a regroup ability used only on purpose
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] Half-troll, strength 8; its assignment trigger wants more companions than minions
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, strength 9, +2 at an underground site
					put("troop", "1_177");       // Goblin Patrol Troop: a [moria] Orc, strength 13, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: an [orc] condition, cheated into the support area

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("boromir", "1_96");      // Boromir, Lord of Gondor: strength 7, vitality 3; not overwhelmed unless tripled
				}},
				// Sites 4 and 5, Balin's Tomb and The Bridge of Khazad-dum, are underground; sites 1 to 3 are not.
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetKingScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("halls", "11_112");

					put("miscreant", "11_131");  // Orc Miscreant: [orc] Orc, strength 7, vitality 3; a regroup ability used only on purpose

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4
				}},
				// Site 3, The Dimholt 8_117, is underground and a sanctuary; sites 1 and 2 are neither.
				VirtualTableScenario.KingSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ConqueredHallsStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Conquered Halls
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Make an [orc] minion strength +1 for each underground site you can spot.
		*/

		var scn = GetScenario();

		var halls = scn.GetFreepsCard("halls");

		assertEquals("Conquered Halls", halls.getBlueprint().getTitle());
		assertNull(halls.getBlueprint().getSubtitle());
		assertFalse(halls.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, halls.getBlueprint().getSide());
		assertEquals(Culture.ORC, halls.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, halls.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(halls, Timeword.SKIRMISH));
		assertEquals(1, halls.getBlueprint().getTwilightCost());
	}

	@Test
	public void AtSite5AnOrcMinionIsStrengthPlusTwoUntilThatSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetShadowCard("halls");
		var miscreant = scn.GetShadowCard("miscreant");
		var halftroll = scn.GetShadowCard("halftroll");
		var denizen = scn.GetShadowCard("denizen");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(halls);
		scn.MoveMinionsToTable(miscreant, halftroll, denizen, troop);
		scn.MoveCardsToSupportArea(beckoning);
		assertInHand(halls);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, miscreant}, new PhysicalCardImpl[]{aragorn, halftroll});
		// The Shadow player puts the Troop on Boromir too and leaves the Denizen unassigned.
		scn.ShadowAssignToMinions(boromir, troop);

		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(8, scn.GetStrength(halftroll));
		assertEquals(7, scn.GetStrength(miscreant));
		assertEquals(11, scn.GetStrength(denizen)); // 9 + 2 at an underground site
		assertTrue(scn.ShadowPlayAvailable(halls));

		scn.ShadowPlayCard(halls);
		// An [orc] minion in this skirmish, one waiting for his own and one assigned to none are offered;
		// the [moria] Orc in this skirmish and the [orc] condition are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(miscreant, halftroll, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, beckoning));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(halftroll);

		// Two underground sites among the five on the path: 8 + 1 + 1.
		assertEquals(10, scn.GetStrength(halftroll));
		assertEquals(7, scn.GetStrength(miscreant));
		assertEquals(11, scn.GetStrength(denizen));
		assertInDiscard(halls);

		// Still +2 at the next decision of this skirmish.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(halftroll));

		scn.PassSkirmishActions();

		// Gone in the next skirmish: each skirmish is a phase of its own.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(8, scn.GetStrength(halftroll));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AtSite4ItIsNotPlayedInTheAssignmentPhaseAndMakesAnOrcMinionStrengthPlusOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetShadowCard("halls");
		var miscreant = scn.GetShadowCard("miscreant");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.SetShadowHand(halls);
		scn.MoveMinionsToTable(miscreant);
		assertInHand(halls);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertEquals(4, scn.GetCurrentSiteNumber());
		scn.FreepsPass();

		// The phase next door: the Shadow player's assignment actions.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(halls));
		scn.ShadowPass();

		scn.FreepsAssignToMinions(boromir, miscreant);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(miscreant));
		assertTrue(scn.ShadowPlayAvailable(halls));
		scn.ShadowPlayCard(halls);

		// One underground site, Balin's Tomb, among the four on the path: 7 + 1.
		assertEquals(8, scn.GetStrength(miscreant));
		assertInDiscard(halls);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnUndergroundSiteThatIsASanctuaryIsCountedToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetKingScenario();

		var halls = scn.GetShadowCard("halls");
		var miscreant = scn.GetShadowCard("miscreant");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.SetShadowHand(halls);
		scn.MoveMinionsToTable(miscreant);
		assertInHand(halls);
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(aragorn, miscreant);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertEquals(7, scn.GetStrength(miscreant));
		scn.ShadowPlayCard(halls);

		// The Dimholt, underground and a sanctuary, is the one underground site: 7 + 1.
		assertEquals(8, scn.GetStrength(miscreant));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithNoOrcMinionOnTheTableItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var halls = scn.GetShadowCard("halls");
		var troop = scn.GetShadowCard("troop");
		var beckoning = scn.GetShadowCard("beckoning");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.SetShadowHand(halls);
		// A [moria] Orc is the only minion, and an [orc] condition the only [orc] card.
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(beckoning);
		assertInHand(halls);
		assertInZone(Zone.SUPPORT, beckoning);
		scn.SkipToAssignments();
		assertEquals(5, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);
		scn.FreepsPass();

		// Two underground sites could be counted, but no [orc] minion can be made stronger.
		assertEquals(13, scn.GetStrength(troop));
		assertTrue(scn.ShadowPlayAvailable(halls));
		scn.ShadowPlayCard(halls);

		assertInDiscard(halls);
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
