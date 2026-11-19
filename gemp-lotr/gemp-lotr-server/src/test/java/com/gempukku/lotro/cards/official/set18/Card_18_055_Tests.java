package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_055_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ranger", "18_55");
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man, vitality 3, no defender of his own; a skirmish ability used only on purpose
					put("aragorn", "1_89");       // Aragorn, Ranger of the North: [gondor] Man; his own defender ability used only on purpose
					put("merchant", "7_97");      // Gondorian Merchant: the [gondor] Man ally, vitality 1; his ability needs initiative
					put("cloak", "15_68");        // Ranger's Cloak: vitality +1 on a [gondor] Man; its texts are about skirmishes
					put("pippin", "103_31");      // Pippin, Trollslayer: a [gondor] Hobbit, the [gondor] companion who is no Man
					put("guard", "5_83");         // Household Guard: a [rohan] Man, the Man who is not [gondor]
					put("gimli", "1_13");         // Gimli, Son of Glóin: a Dwarf companion to be healed; a skirmish ability used only on purpose
					put("barliman", "1_70");      // Barliman Butterbur: a Man ally, vitality 2; a fellowship ability used only on purpose
					put("troop", "1_177");        // Goblin Patrol Troop: no text; only there so that a maneuver phase is played
					put("runner", "1_178");       // Goblin Runner: [moria] Orc, strength 5; its play text is not reached by a cheat
					put("savage", "1_151");       // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3, damage +1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RangeroftheNorthStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Ranger of the North
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: Bearer must be a [gondor] Man.<br><b>Maneuver:</b> If the fellowship is in region 1 or region 2, you may exert bearer to make him or her <b>defender +1</b> until the regroup phase.<br><b>Regroup:</b> Discard this to heal a companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ranger");

		assertEquals("Ranger of the North", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlaysOnAGondorManCompanionOrAllyButNotOnAGondorHobbitOrARohanMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var merchant = scn.GetFreepsCard("merchant");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(boromir, aragorn, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(ranger);

		scn.StartGame();

		assertInHand(ranger);
		assertTrue(scn.FreepsPlayAvailable(ranger));
		scn.FreepsPlayCard(ranger);

		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, aragorn, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(boromir);

		assertAttachedTo(ranger, boromir);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void InRegionOneManeuverExertsBearerToMakeHimDefenderPlus1UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		var aragorn = scn.GetFreepsCard("aragorn");
		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(boromir, aragorn);
		scn.AttachCardsTo(boromir, ranger);

		scn.StartGame();
		assertAttachedTo(ranger, boromir);
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 2: region 1.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(ranger));
		scn.FreepsUseCardAction(ranger);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		// Aragorn, a [gondor] Man who does not bear it, gains nothing.
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DEFENDER));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, runner}, new PhysicalCardImpl[]{boromir, savage});
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// In the skirmish after one already resolved: still defender +1.
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		scn.PassSkirmishActions();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
	}

	@Test
	public void InRegionTwoAtItsLastSiteTheManeuverAbilityIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, ranger);

		scn.StartGame();
		assertAttachedTo(ranger, boromir);
		scn.SkipToSite(5);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 6: region 2.
		assertEquals(6, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(ranger));
		scn.FreepsUseCardAction(ranger);

		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetKeywordCount(boromir, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void InRegionThreeNeitherAbilityIsOfferedInTheManeuverPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.AttachCardsTo(boromir, ranger);

		scn.StartGame();
		assertAttachedTo(ranger, boromir);
		scn.SkipToSite(6);
		// A wound for the regroup ability to heal, put on after the sanctuary at site 6.
		scn.AddWoundsToChar(gimli, 1);
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		// Site 7: region 3. Boromir can exert, and Gimli is wounded.
		assertEquals(7, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.FreepsActionAvailable(ranger));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ManeuverAbilityIsNotOfferedWhileBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, ranger);
		// Vitality 3 with 2 wounds: exhausted.
		scn.AddWoundsToChar(boromir, 2);

		scn.StartGame();
		assertAttachedTo(ranger, boromir);
		assertEquals(2, scn.GetWoundsOn(boromir));
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(2, scn.GetCurrentSiteNumber());
		assertFalse(scn.FreepsActionAvailable(ranger));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AnAllyBearerExertsToBeMadeDefenderPlus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ranger = scn.GetFreepsCard("ranger");
		var merchant = scn.GetFreepsCard("merchant");
		var cloak = scn.GetFreepsCard("cloak");
		scn.MoveAlliesToTable(merchant);
		// The Cloak makes his vitality 2, so that he can exert once.
		scn.AttachCardsTo(merchant, cloak, ranger);

		scn.StartGame();
		assertAttachedTo(ranger, merchant);
		assertAttachedTo(cloak, merchant);
		scn.MoveMinionsToTable("troop");
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DEFENDER));
		assertTrue(scn.FreepsActionAvailable(ranger));
		scn.FreepsUseCardAction(ranger);

		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(1, scn.GetKeywordCount(merchant, Keyword.DEFENDER));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void RegroupDiscardsItToHealAWoundedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var barliman = scn.GetFreepsCard("barliman");
		scn.MoveCompanionsToTable(boromir, gimli);
		scn.MoveAlliesToTable(barliman);
		scn.AttachCardsTo(boromir, ranger);
		scn.AddWoundsToChar(boromir, 1);
		scn.AddWoundsToChar(gimli, 2);
		scn.AddWoundsToChar(barliman, 1);

		scn.StartGame();
		assertAttachedTo(ranger, boromir);
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(2, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertEquals(0, scn.GetWoundsOn(frodo));
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsActionAvailable(ranger));
		scn.FreepsUseCardAction(ranger);

		assertInDiscard(ranger);
		// Its bearer and Gimli are wounded; Frodo is not; Barliman is an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, gimli));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(frodo, barliman));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(gimli);

		// One wound of his two.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(barliman));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNobodyWoundedTheRegroupAbilityIsStillOfferedAndDiscardsItForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ranger = scn.GetFreepsCard("ranger");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(boromir);
		scn.AttachCardsTo(boromir, ranger);

		scn.StartGame();
		assertAttachedTo(ranger, boromir);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.FreepsActionAvailable(ranger));
		scn.FreepsUseCardAction(ranger);

		assertInDiscard(ranger);
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}
}
