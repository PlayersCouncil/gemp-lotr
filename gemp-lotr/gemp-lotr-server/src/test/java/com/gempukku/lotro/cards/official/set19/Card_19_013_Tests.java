package com.gempukku.lotro.cards.official.set19;

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

public class Card_19_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("boromir", "19_13");
					put("merry", "1_302");        // Merry, Friend to Sam: an unbound [shire] Hobbit, cost 1; a skirmish ability never used
					put("pippin", "103_31");      // Pippin, Trollslayer: an unbound [gondor] Hobbit; a skirmish ability never used
					put("sam", "1_311");          // Sam, Son of Hamfast: a Ring-bound Hobbit; abilities used only on purpose
					put("bounder", "1_286");      // Bounder: a Hobbit ally (no ally is unbound); a skirmish ability never used
					put("gimli", "1_13");         // Gimli, Son of Gloin: an unbound companion who is no Hobbit; a skirmish ability never used
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: an unbound Man, cost 4; a maneuver ability never used

					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("slayer", "3_97");        // Orc Slayer: [sauron] Orc, strength 5, vitality 2; a regroup ability never used
					put("seeker", "4_195");       // Uruk Seeker: fierce, strength 6, vitality 2; its bonus is against a bearer of a search card only
					put("pursuer", "4_190");      // Uruk Pursuer: fierce, strength 5, vitality 3; its texts are about unbound Hobbits discarded or killed
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BoromirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 19
		 * Name: Boromir, Destined Guide
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 3
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 7
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: Fellowship. Ranger.
		 * 	While you can spot two unbound Hobbits, Boromir's twilight cost is -1.
		 * 	Each time Boromir wins a skirmish, you may exert an unbound Hobbit to discard each minion he is skirmishing from play.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("boromir");

		assertEquals("Boromir", card.getBlueprint().getTitle());
		assertEquals("Destined Guide", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.FELLOWSHIP));
		assertTrue(scn.HasKeyword(card, Keyword.RANGER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(7, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getResistance());
	}

	@Test
	public void WhileTwoUnboundHobbitsCanBeSpottedBoromirCostsOneLess() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(boromir, aragorn, merry);
		// One unbound Hobbit; two Ring-bound Hobbits (Frodo and Sam), a Hobbit ally and an unbound Dwarf beside him.
		scn.MoveCompanionsToTable(pippin, sam, gimli);
		scn.MoveAlliesToTable(bounder);

		assertInZone(Zone.HAND, boromir, aragorn, merry);

		scn.StartGame();

		assertEquals(3, scn.GetTwilightCost(boromir));
		assertEquals(4, scn.GetTwilightCost(aragorn));
		assertEquals(0, scn.GetTwilight());

		scn.FreepsPlayCard(merry);

		assertInZone(Zone.FREE_CHARACTERS, merry);
		assertEquals(2, scn.GetTwilightCost(boromir));
		// Another companion in hand keeps his cost.
		assertEquals(4, scn.GetTwilightCost(aragorn));

		int twilight = scn.GetTwilight();

		scn.FreepsPlayCard(boromir);

		assertInZone(Zone.FREE_CHARACTERS, boromir);
		// 3 - 1 for Boromir
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhenBoromirWinsAnUnboundHobbitMayExertToDiscardEachMinionHeIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var slayer = scn.GetShadowCard("slayer");
		var seeker = scn.GetShadowCard("seeker");
		scn.MoveCompanionsToTable(boromir, merry, pippin, sam, aragorn);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Boromir 7 + 5 beats Uruk Savage and Orc Slayer (5 + 5) without overwhelming them.
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 5));
		scn.FreepsPass();
		scn.MoveMinionsToTable(savage, slayer, seeker);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { boromir, savage }, new PhysicalCardImpl[] { aragorn, seeker });
		scn.ShadowAssignToMinions(boromir, slayer);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.PassSkirmishActions();

		// Each minion takes a wound and lives.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(1, scn.GetWoundsOn(slayer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));

		scn.FreepsAcceptOptionalTrigger();
		// The unbound Hobbits of two cultures; not a Ring-bound Hobbit, a Hobbit ally or an unbound Man.
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertTrue(scn.FreepsHasCardChoiceAvailable(merry, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(sam, frodo, bounder, aragorn, boromir));

		scn.FreepsChooseCard(pippin);

		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		// Both minions of his skirmish, an Uruk-hai and an Orc; not the minion waiting for Aragorn.
		assertInDiscard(savage, slayer);
		assertInZone(Zone.SHADOW_CHARACTERS, seeker);
		assertTrue(scn.IsCharAssignedAgainst(aragorn, seeker));
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish"));
	}

	@Test
	public void TheDiscardMayBeDeclinedAndIsOfferedAgainAtTheNextWin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var sam = scn.GetFreepsCard("sam");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(boromir, merry, sam);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(pursuer);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// Boromir (7) beats Uruk Pursuer (5).
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));

		scn.FreepsDeclineOptionalTrigger();
		assertInZone(Zone.SHADOW_CHARACTERS, pursuer);
		assertEquals(0, scn.GetWoundsOn(merry));

		// The Pursuer is fierce: Boromir beats him a second time, and the offer is made again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(boromir, pursuer);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(boromir));

		scn.FreepsAcceptOptionalTrigger();
		// Merry the only unbound Hobbit: he exerts unasked.
		assertEquals(1, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertInDiscard(pursuer);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenBoromirLosesASkirmishNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(boromir, merry);

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(troop);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, troop);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(0, scn.GetWoundsOn(boromir));
		scn.PassSkirmishActions();

		// Goblin Patrol Troop (13) beats Boromir (7) without overwhelming him.
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithEveryUnboundHobbitExhaustedNothingIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var boromir = scn.GetFreepsCard("boromir");
		var merry = scn.GetFreepsCard("merry");
		var pippin = scn.GetFreepsCard("pippin");
		var sam = scn.GetFreepsCard("sam");
		var bounder = scn.GetFreepsCard("bounder");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir, merry, pippin, sam);
		scn.MoveAlliesToTable(bounder);

		scn.StartGame();
		// Vitality 4 each: three wounds leave both exhausted. Sam and the Bounder could still exert.
		scn.AddWoundsToChar(merry, 3);
		scn.AddWoundsToChar(pippin, 3);

		assertEquals(3, scn.GetWoundsOn(merry));
		assertEquals(3, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetWoundsOn(bounder));

		scn.FreepsPass();
		scn.MoveMinionsToTable(savage);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, savage);
		scn.FreepsResolveSkirmish(boromir);
		scn.PassSkirmishActions();

		// Boromir (7) beats Uruk Savage (5).
		assertEquals(1, scn.GetWoundsOn(savage));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable());
		assertInZone(Zone.SHADOW_CHARACTERS, savage);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
