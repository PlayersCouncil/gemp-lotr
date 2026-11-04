package com.gempukku.lotro.cards.official.set11;

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

public class Card_11_203_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("swarming", "11_203");
					put("swarming2", "11_203");
					// Murderous Uruk: an [uruk-hai] Uruk-hai minion; damage +1 and muster (a regroup trigger) only.
					put("murderous", "11_195");
					// Saruman, Master of the White Hand: an [uruk-hai] minion who is a Wizard and no Uruk-hai; each
					// companion is resistance -1, and a skirmish ability used only on purpose.
					put("saruman", "17_116");
					// Cirith Ungol Guard: a [sauron] Uruk-hai, damage +1 and no other text: an Uruk-hai of another culture.
					put("guard", "10_81");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8; a maneuver ability only
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7; a skirmish ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SwarmingUrukStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 11
		 * Name: Swarming Uruk
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 3
		 * Type: Minion
		 * Subtype: Uruk-hai
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 5
		 * Game Text: <b>Damage +1</b>.<br>While you can spot another [uruk-hai] minion assigned to a skirmish, this minion cannot take wounds.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("swarming");

		assertEquals("Swarming Uruk", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.MINION, card.getBlueprint().getCardType());
		assertEquals(Race.URUK_HAI, card.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(card, Keyword.DAMAGE));
		assertEquals(1, scn.GetKeywordCount(card, Keyword.DAMAGE));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(9, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
		assertEquals(5, card.getBlueprint().getSiteNumber());
	}

	@Test
	public void WhileAnotherUrukHaiMinionWaitsForItsSkirmishItTakesNoWoundForLosing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var swarming = scn.GetShadowCard("swarming");
		var murderous = scn.GetShadowCard("murderous");

		scn.StartGame();
		scn.MoveMinionsToTable(swarming, murderous);
		// Aragorn made 12 by hand: he beats the Uruk's 9 and does not overwhelm it (12 is less than 18).
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, swarming }, new PhysicalCardImpl[] { boromir, murderous });
		assertTrue(scn.IsCharAssignedAgainst(boromir, murderous));

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, swarming));
		assertEquals(12, scn.GetStrength(aragorn));
		assertEquals(9, scn.GetStrength(swarming));
		assertEquals(0, scn.GetWoundsOn(swarming));

		scn.PassSkirmishActions();
		// It lost, and the Murderous Uruk is still assigned, waiting for its skirmish: no wound.
		assertEquals(0, scn.GetWoundsOn(swarming));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.SHADOW_CHARACTERS, swarming);
		assertTrue(scn.IsCharAssignedAgainst(boromir, murderous));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void AnAssignedUrukHaiMinionWhoIsNoUrukHaiAlsoSparesItTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var swarming = scn.GetShadowCard("swarming");
		var saruman = scn.GetShadowCard("saruman");

		scn.StartGame();
		scn.MoveMinionsToTable(swarming, saruman);
		// Aragorn made 12 by hand: he beats the Uruk's 9 and does not overwhelm it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, swarming }, new PhysicalCardImpl[] { boromir, saruman });

		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, swarming));
		assertEquals(0, scn.GetWoundsOn(swarming));

		scn.PassSkirmishActions();
		// Saruman, an [uruk-hai] Wizard, is still assigned: no wound.
		assertEquals(0, scn.GetWoundsOn(swarming));
		assertInZone(Zone.SHADOW_CHARACTERS, swarming);
		assertTrue(scn.IsCharAssignedAgainst(boromir, saruman));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}

	@Test
	public void EachCopySparesTheOtherUntilItsOwnSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var swarming = scn.GetShadowCard("swarming");
		var swarming2 = scn.GetShadowCard("swarming2");

		scn.StartGame();
		scn.MoveMinionsToTable(swarming, swarming2);
		// Both companions made 12 by hand: each beats an Uruk's 9 and does not overwhelm it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));
		scn.ApplyAdHocModifier(new StrengthModifier(null, boromir, null, 5));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, swarming }, new PhysicalCardImpl[] { boromir, swarming2 });

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(swarming));
		assertEquals(0, scn.GetWoundsOn(swarming2));
		scn.PassSkirmishActions();
		// The second copy is still assigned: the first takes no wound.
		assertEquals(0, scn.GetWoundsOn(swarming));

		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.IsCharSkirmishingAgainst(boromir, swarming2));
		assertEquals(12, scn.GetStrength(boromir));
		scn.PassSkirmishActions();
		// The first copy's skirmish is over and it is no longer assigned (CR 4.2 "skirmish phase(s)"): nothing
		// spares the second, which takes the wound for losing.
		assertEquals(1, scn.GetWoundsOn(swarming2));
		assertEquals(0, scn.GetWoundsOn(swarming));
		assertInZone(Zone.SHADOW_CHARACTERS, swarming, swarming2);
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
	}

	@Test
	public void ItTakesTheWoundWhenOnlyItselfAnUnassignedUrukHaiMinionAndAnUrukHaiOfAnotherCultureCouldBeSpotted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		scn.MoveCompanionsToTable(aragorn, boromir);

		var swarming = scn.GetShadowCard("swarming");
		var murderous = scn.GetShadowCard("murderous");
		var guard = scn.GetShadowCard("guard");

		scn.StartGame();
		scn.MoveMinionsToTable(swarming, murderous, guard);
		// Aragorn made 12 by hand: he beats the Uruk's 9 and does not overwhelm it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 4));

		scn.SkipToAssignments();
		// The Murderous Uruk is left unassigned, and the Shadow player assigns it to nobody.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[] { aragorn, swarming }, new PhysicalCardImpl[] { boromir, guard });
		scn.ShadowDeclineAssignments();
		assertFalse(scn.IsCharAssigned(murderous));
		assertTrue(scn.IsCharAssignedAgainst(boromir, guard));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(swarming));
		scn.PassSkirmishActions();

		// Swarming Uruk itself is assigned, but is not "another"; the Murderous Uruk is not assigned; the [sauron]
		// Guard is assigned and waiting but is not [uruk-hai]: the wound is taken.
		assertEquals(1, scn.GetWoundsOn(swarming));
		assertInZone(Zone.SHADOW_CHARACTERS, swarming);
		assertTrue(scn.IsCharAssignedAgainst(boromir, guard));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
	}
}
