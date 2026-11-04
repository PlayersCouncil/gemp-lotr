package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_003_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stratagem", "17_3");
					// The only Dwarf hunters in print are two Gimlis, never in play together.
					put("eager", "15_5");       // Gimli, Eager Hunter: Dwarf companion, damage +1, hunter 3, no other text
					// Gimli, Sprinter: Dwarf companion, damage +1, hunter 1; his trigger needs him to win a skirmish
					put("sprinter", "18_1");
					put("sonofgloin", "1_13");  // Gimli, Son of Gloin: a Dwarf who is no hunter
					// Dunedain of the South: [gondor] Man, ranger, hunter 1; his trigger needs him to win a skirmish
					put("dunedain", "15_59");
					// Seeking Uruk: a hunter minion, vitality 2; strength +1 for each hunter in a fierce skirmish only
					put("seeker", "15_169");
					put("troop", "1_177");      // Goblin Patrol Troop: vitality 3, no text
					put("runner", "1_178");     // Goblin Runner: vitality 1, cannot exert
					// [dwarven] support-area conditions that carry [dwarven] tokens; their abilities are not used
					put("stout", "4_57");       // Stout and Strong
					put("balin", "17_2");       // Balin Avenged
					put("heirs", "13_69");      // Heirs of Gondor: carries [gondor] tokens
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DwarvenStratagemStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Dwarven Stratagem
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Dwarven
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: Spot a Dwarf hunter and choose one: exert a minion for each hunter you can spot or reinforce a [dwarven] token for each hunter you can spot.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stratagem");

		assertEquals("Dwarven Stratagem", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.DWARVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
        assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void CannotBePlayedWithoutADwarfHunter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stratagem = scn.GetFreepsCard("stratagem");
		var sonofgloin = scn.GetFreepsCard("sonofgloin");
		var dunedain = scn.GetFreepsCard("dunedain");
		scn.MoveCompanionsToTable(sonofgloin, dunedain);
		scn.MoveCardsToHand(stratagem);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertInHand(stratagem);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sonofgloin, troop);
		scn.FreepsResolveSkirmish(sonofgloin);

		// A Dwarf who is no hunter, and a hunter who is no Dwarf.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(stratagem));
	}

	@Test
	public void ExertsAMinionForEachHunterOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stratagem = scn.GetFreepsCard("stratagem");
		var eager = scn.GetFreepsCard("eager");
		var dunedain = scn.GetFreepsCard("dunedain");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(eager, dunedain);
		scn.MoveCardsToSupportArea(stout);
		scn.MoveCardsToHand(stratagem);

		var seeker = scn.GetShadowCard("seeker");
		var troop = scn.GetShadowCard("troop");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddTokensToCard(stout, 1);
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInHand(stratagem);
		scn.MoveMinionsToTable(seeker, troop, runner);
		scn.SkipToAssignments();
		// The Seeking Uruk and the Runner are left unassigned.
		scn.FreepsAssignToMinions(eager, troop);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(eager);

		assertTrue(scn.FreepsPlayAvailable(stratagem));
		scn.FreepsPlayCard(stratagem);

		// Both can be done: the choice is asked.
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		assertTrue(scn.FreepsChoiceAvailable("Reinforce"));
		scn.FreepsChoose("Exert");

		// Three hunters: Gimli, the Dunedain and the Seeking Uruk.  Each exertion is a choice of a minion; the
		// Runner, of vitality 1, cannot exert.
		assertTrue(scn.FreepsHasCardChoiceAvailable(seeker, troop));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(runner));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(seeker);

		// The Seeking Uruk is exhausted now: the other two go to the Troop unasked.
		assertEquals(1, scn.GetWoundsOn(seeker));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInDiscard(stratagem);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void ReinforcesADwarvenTokenForEachHunterOnAnyDwarvenCard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stratagem = scn.GetFreepsCard("stratagem");
		var eager = scn.GetFreepsCard("eager");
		var dunedain = scn.GetFreepsCard("dunedain");
		var stout = scn.GetFreepsCard("stout");
		var balin = scn.GetFreepsCard("balin");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(eager, dunedain);
		scn.MoveCardsToSupportArea(stout, balin, heirs);
		scn.MoveCardsToHand(stratagem);

		var seeker = scn.GetShadowCard("seeker");

		scn.StartGame();
		scn.AddTokensToCard(stout, 1);
		scn.AddTokensToCard(balin, 1);
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertEquals(1, scn.GetCultureTokensOn(balin));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertInHand(stratagem);
		scn.MoveMinionsToTable(seeker);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eager, seeker);
		scn.FreepsResolveSkirmish(eager);

		scn.FreepsPlayCard(stratagem);
		assertTrue(scn.FreepsChoiceAvailable("Exert"));
		scn.FreepsChoose("Reinforce");

		// Three hunters, Gimli, the Dunedain and the minion Seeking Uruk: three tokens, each on a [dwarven] card
		// chosen in turn; the [gondor] token is none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(stout, balin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(heirs));
		scn.FreepsChooseCard(stout);
		assertTrue(scn.FreepsHasCardChoiceAvailable(stout, balin));
		scn.FreepsChooseCard(balin);
		assertTrue(scn.FreepsHasCardChoiceAvailable(stout, balin));
		scn.FreepsChooseCard(stout);

		assertEquals(3, scn.GetCultureTokensOn(stout));
		assertEquals(2, scn.GetCultureTokensOn(balin));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertEquals(0, scn.GetWoundsOn(seeker));
		assertInDiscard(stratagem);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoDwarvenTokenToReinforceTheMinionsAreExertedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stratagem = scn.GetFreepsCard("stratagem");
		var sprinter = scn.GetFreepsCard("sprinter");
		var dunedain = scn.GetFreepsCard("dunedain");
		var heirs = scn.GetFreepsCard("heirs");
		scn.MoveCompanionsToTable(sprinter, dunedain);
		scn.MoveCardsToSupportArea(heirs);
		scn.MoveCardsToHand(stratagem);

		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		// A token of another culture only.
		scn.AddTokensToCard(heirs, 1);
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertInHand(stratagem);
		scn.MoveMinionsToTable(troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sprinter, troop);
		scn.FreepsResolveSkirmish(sprinter);

		assertTrue(scn.FreepsPlayAvailable(stratagem));
		scn.FreepsPlayCard(stratagem);

		// Two hunters, Gimli and the Dunedain, and the Troop is the only minion.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetCultureTokensOn(heirs));
		assertInDiscard(stratagem);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WithNoMinionAbleToExertTheTokenIsReinforcedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stratagem = scn.GetFreepsCard("stratagem");
		var eager = scn.GetFreepsCard("eager");
		var stout = scn.GetFreepsCard("stout");
		scn.MoveCompanionsToTable(eager);
		scn.MoveCardsToSupportArea(stout);
		scn.MoveCardsToHand(stratagem);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.AddTokensToCard(stout, 1);
		assertEquals(1, scn.GetCultureTokensOn(stout));
		assertInHand(stratagem);
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eager, runner);
		scn.FreepsResolveSkirmish(eager);

		scn.FreepsPlayCard(stratagem);

		// Gimli is the one hunter, and the Runner cannot exert.
		assertFalse(scn.FreepsDecisionAvailable("Choose action to perform"));
		assertEquals(2, scn.GetCultureTokensOn(stout));
		assertEquals(0, scn.GetWoundsOn(runner));
		assertInDiscard(stratagem);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
