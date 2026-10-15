package com.gempukku.lotro.cards.official.set01;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_01_102_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("strike", "1_102");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: [gondor] artifact hand weapon, vitality +1 and no strength; a response to a wound on its bearer
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit, strength 3; his text is about [shire] tales
					put("sword", "1_299");      // Hobbit Sword: [shire] possession hand weapon, strength +2; "Bearer must be a Hobbit" is its whole text
					put("boromir", "1_97");     // Boromir, Son of Denethor: [gondor] Man, strength 7; a skirmish ability used only on purpose
					put("bow", "7_94");         // Gondor Bow: [gondor] RANGED weapon on a [gondor] Man; +1 only on a knight, a skirmish ability used only on purpose
					put("gimli", "1_13");       // Gimli, Son of Glóin: [dwarven] Dwarf, strength 6, damage +1; a skirmish ability used only on purpose
					put("axe", "1_9");          // Dwarven Axe: [dwarven] hand weapon, strength +2; its trigger answers a minion losing to its bearer
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man who is an ally, strength 6; a maneuver ability used only on purpose
					put("gsword", "4_134");     // Sword of Gondor: [gondor] hand weapon, strength +2; damage +1 only while its bearer skirmishes a Man or a roaming minion

					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3; "Damage +1." is its whole text
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1; cheated in, so its text does nothing
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DaggerStrikeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 1
		 * Name: Dagger Strike
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make a [gondor] or [shire] companion bearing a hand weapon strength +2 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("strike");

		assertEquals("Dagger Strike", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAGondorCompanionBearingAHandWeaponStrengthPlus2AndDamagePlus1ForHisSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var sword = scn.GetFreepsCard("sword");
		var boromir = scn.GetFreepsCard("boromir");
		var bow = scn.GetFreepsCard("bow");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var merchant = scn.GetFreepsCard("merchant");
		var gsword = scn.GetFreepsCard("gsword");
		scn.MoveCompanionsToTable(aragorn, pippin, boromir, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(pippin, sword);
		scn.AttachCardsTo(boromir, bow);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(merchant, gsword);
		scn.MoveCardsToHand(strike);

		var party = scn.GetShadowCard("party");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(party, runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, party},
				new PhysicalCardImpl[]{pippin, runner});
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
		assertEquals(0, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsPlayAvailable(strike));

		scn.FreepsPlayCard(strike);
		// A [gondor] Man with an artifact and a [shire] Hobbit with a possession, the Hobbit waiting for a skirmish of
		// his own.  Not a [gondor] companion whose only weapon is a ranged one, not a companion of another culture
		// with a hand weapon, not a [gondor] ally with a hand weapon, and not Frodo, a [shire] companion with none.
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, gimli, merchant, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);

		assertEquals(10, scn.GetStrength(aragorn));
		assertEquals(1, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertInDiscard(strike);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// 10 against the Raiding Party's 9: Aragorn wins, and places one wound and one more for damage +1.
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(party));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// In the next skirmish, a phase of its own, Aragorn is himself again.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
	}

	@Test
	public void AShireCompanionWaitingForALaterSkirmishMayBeChosenAndIsHimselfAgainInHisOwn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, pippin);
		scn.AttachCardsTo(aragorn, narsil);
		scn.AttachCardsTo(pippin, sword);
		scn.MoveCardsToHand(strike);

		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, runner2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{aragorn, runner},
				new PhysicalCardImpl[]{pippin, runner2});
		scn.FreepsResolveSkirmish(aragorn);

		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.FreepsPlayCard(strike);
		assertTrue(scn.FreepsHasCardChoiceAvailable(aragorn, pippin));
		scn.FreepsChooseCard(pippin);

		// Pippin is not in this skirmish, and the text names none.
		assertEquals(7, scn.GetStrength(pippin));
		assertEquals(1, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(aragorn));
		assertEquals(0, scn.GetKeywordCount(aragorn, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		assertInDiscard(runner);

		// The event's effect lasted the skirmish phase it was played in: in his own skirmish Pippin is 3 + 2 again.
		scn.FreepsResolveSkirmish(pippin);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(5, scn.GetStrength(pippin));
		assertEquals(0, scn.GetKeywordCount(pippin, Keyword.DAMAGE));
	}

	@Test
	public void CannotBePlayedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var aragorn = scn.GetFreepsCard("aragorn");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, narsil);
		scn.MoveCardsToHand(strike);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertInHand(strike);
		assertFalse(scn.FreepsPlayAvailable(strike));

		// The same board one phase on.
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(aragorn, runner);
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.FreepsPlayAvailable(strike));
	}

	@Test
	public void IsPlayedForNothingWithNoGondorOrShireCompanionBearingAHandWeapon() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var strike = scn.GetFreepsCard("strike");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var boromir = scn.GetFreepsCard("boromir");
		var bow = scn.GetFreepsCard("bow");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var merchant = scn.GetFreepsCard("merchant");
		var gsword = scn.GetFreepsCard("gsword");
		scn.MoveCompanionsToTable(pippin, boromir, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(boromir, bow);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(merchant, gsword);
		scn.MoveCardsToHand(strike);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(boromir, runner);
		scn.FreepsResolveSkirmish(boromir);

		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(merchant));
		assertEquals(3, scn.GetStrength(pippin));
		assertTrue(scn.FreepsPlayAvailable(strike));

		// A [gondor] companion with a ranged weapon only, a [dwarven] companion and a [gondor] ally with hand weapons,
		// and two [shire] Hobbits with no weapon: the event is played, and nobody is made stronger.
		scn.FreepsPlayCard(strike);
		assertInDiscard(strike);
		assertEquals(7, scn.GetStrength(boromir));
		assertEquals(0, scn.GetKeywordCount(boromir, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(gimli));
		assertEquals(1, scn.GetKeywordCount(gimli, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(merchant));
		assertEquals(0, scn.GetKeywordCount(merchant, Keyword.DAMAGE));
		assertEquals(3, scn.GetStrength(pippin));
		assertEquals(0, scn.GetKeywordCount(frodo, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
