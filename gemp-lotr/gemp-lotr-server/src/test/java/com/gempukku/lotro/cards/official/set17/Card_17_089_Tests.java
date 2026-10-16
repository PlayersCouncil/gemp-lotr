package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_089_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "17_89");
					put("warg2", "17_89");
					put("chief", "15_120");     // Veteran War Chief: [orc] Orc of strength 10; a skirmish ability only
					put("denizen", "11_117");   // Denizen of the Black Pit: [orc] Orc of strength 10, vitality 3; +2 only at an underground site
					put("veteran", "15_96");    // Battlefield Veteran: [orc] Orc of strength 11; a skirmish ability only
					put("halftroll", "14_12");  // Half-troll of Far Harad: an [orc] minion who is no Orc, strength 8; a start-of-assignment trigger
					put("runner", "1_178");     // Goblin Runner: a [moria] Orc, strength 5; may add (2) as it is played

					put("gimli", "1_13");       // Gimli, Son of Gloin: a Dwarf companion, strength 6, damage +1
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a Man companion, strength 8
					put("thrarin", "1_27");     // Thrarin, Dwarven Smith: a Dwarf ally
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// An [orc] Orc to bear the Warg, a Hobbit ally at his home site and a Hobbit companion.
	protected VirtualTableScenario GetHobbitAllyAndCompanionScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("warg", "17_89");
					put("goblin", "11_137");
					put("bounder", "1_286");
					put("sam", "1_311");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void RelentlessWargStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Relentless Warg
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 3
		 * Type: Possession
		 * Subtype: Mount
		 * Strength: 2
		 * Vitality: 2
		 * Game Text: Bearer must be an [orc] Orc with strength 10 or less. Bearer is <b>fierce</b>.<br>When you play this possession, spot a race. While bearer is skirmishing a companion of that race, bearer is strength +3 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("warg");

		assertEquals("Relentless Warg", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ORC, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.MOUNT));
		assertEquals(3, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
		assertEquals(2, card.getBlueprint().getVitality());
	}

	@Test
	public void PlaysOnAnOrcOrcOfStrength10OrLessMakesItFierceAndSpotsARace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var thrarin = scn.GetFreepsCard("thrarin");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(thrarin);

		var warg = scn.GetShadowCard("warg");
		var chief = scn.GetShadowCard("chief");
		var denizen = scn.GetShadowCard("denizen");
		var veteran = scn.GetShadowCard("veteran");
		var halftroll = scn.GetShadowCard("halftroll");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		scn.MoveMinionsToTable(chief, denizen, veteran, halftroll, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertFalse(scn.HasKeyword(chief, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(denizen, Keyword.FIERCE));
		assertTrue(scn.ShadowPlayAvailable(warg));

		// Two [orc] Orcs of strength 10; an [orc] Orc of 11, an [orc] Half-troll and a [moria] Orc.
		scn.ShadowPlayCard(warg);
		assertTrue(scn.ShadowHasCardChoiceAvailable(chief, denizen));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(veteran, halftroll, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(chief);
		assertAttachedTo(warg, chief);

		// A race is spotted by a character of it: a companion, an ally or a minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(gimli, thrarin, scn.GetRingBearer(), denizen));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(gimli);

		assertTrue(scn.HasKeyword(chief, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(denizen, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsStrengthPlus3AndDamagePlus1OnlyOnceSkirmishingACompanionOfItsWargsRace() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var warg = scn.GetShadowCard("warg");
		var warg2 = scn.GetShadowCard("warg2");
		var chief = scn.GetShadowCard("chief");
		var denizen = scn.GetShadowCard("denizen");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(warg, warg2);

		scn.StartGame();
		scn.MoveMinionsToTable(chief, denizen, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// The first Warg on the War Chief spots Dwarf (Gimli).
		scn.ShadowPlayCard(warg);
		scn.ShadowChooseCard(chief);
		scn.ShadowChooseCard(gimli);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// The second, on the Denizen (who alone can bear it), spots Man (Aragorn).  Only its own play asks for a
		// race: the first Warg does not ask again.
		scn.ShadowPlayCard(warg2);
		assertAttachedTo(warg2, denizen);
		scn.ShadowChooseCard(aragorn);
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gimli, chief}, new PhysicalCardImpl[]{aragorn, runner});
		scn.ShadowAssignToMinions(gimli, denizen);

		// The War Chief is assigned to Gimli, but is not skirmishing him while Aragorn's skirmish is fought: 10 + 2.
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(12, scn.GetStrength(chief));
		assertEquals(0, scn.GetKeywordCount(chief, Keyword.DAMAGE));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);

		// Skirmishing Gimli, a Dwarf: 10 + 2 + 3 and damage +1.  The Denizen beside him in the same skirmish has
		// its Warg's Man, so it is 10 + 2 and no damage bonus, and the first Warg gives it nothing.
		assertEquals(15, scn.GetStrength(chief));
		assertEquals(1, scn.GetKeywordCount(chief, Keyword.DAMAGE));
		assertEquals(12, scn.GetStrength(denizen));
		assertEquals(0, scn.GetKeywordCount(denizen, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	// "While bearer is skirmishing a companion of that race": the definition counted an ally of that race as well.
	@Test
	public void TheBonusIsForSkirmishingACompanionOfTheRaceAndNotAnAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetHobbitAllyAndCompanionScenario();

		var bounder = scn.GetFreepsCard("bounder");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCompanionsToTable(sam);

		var warg = scn.GetShadowCard("warg");
		var goblin = scn.GetShadowCard("goblin");
		scn.MoveCardsToHand(warg);

		scn.StartGame();
		scn.MoveMinionsToTable(goblin);
		scn.SkipToPhase(Phase.SHADOW);

		// The Goblin is the only [orc] Orc; the race spotted is Sam's, Hobbit.
		scn.ShadowPlayCard(warg);
		assertAttachedTo(warg, goblin);
		scn.ShadowChooseCard(sam);
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Site 2 is Bounder's home.  He is a Hobbit and an ally: the Goblin is 5 + 2 for the Warg and no more.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(bounder, goblin);
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(7, scn.GetStrength(goblin));
		assertFalse(scn.HasKeyword(goblin, Keyword.DAMAGE));

		// 2 against 7: Bounder is killed, and the Goblin, fierce for the Warg, skirmishes again.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, bounder);
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, goblin);
		scn.FreepsResolveSkirmish(sam);

		// Sam is a Hobbit and a companion.
		assertEquals(10, scn.GetStrength(goblin));
		assertTrue(scn.HasKeyword(goblin, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
