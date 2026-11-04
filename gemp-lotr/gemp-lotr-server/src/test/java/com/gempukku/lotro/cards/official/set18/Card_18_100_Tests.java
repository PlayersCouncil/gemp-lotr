package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_100_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("horn", "18_100");
					put("gamling", "18_99");      // Gamling, Dutiful Marshal: [rohan] Man; his skirmish ability is never used
					put("guard", "5_83");         // Household Guard: [rohan] Man, strength 6; Valiant is its whole text
					put("boromir", "1_97");       // Boromir, Son of Denethor: [gondor] Man, strength 7; a skirmish ability used only on purpose
					put("herdsman", "7_247");     // Rohirrim Herdsman: [rohan] Man ally; a regroup ability used only on purpose
					put("merry", "103_85");       // Merry, Master Holbytla: [rohan] Hobbit, a Man only beside 3 [rohan] companions or Eowyn
					put("merry2", "1_302");       // Merry, Friend to Sam: [shire] Hobbit; a skirmish ability used only on purpose
					put("pippin", "1_306");       // Pippin, Friend to Frodo: a Hobbit who is no Merry; his text guards [shire] tales

					put("worker", "15_135");      // Rohan Worker: [rohan] follower, changes no number
					put("worker2", "15_135");
					put("army", "18_47");         // Elendil's Army: [gondor] follower, strength +2 to its bearer
					put("saruman", "17_38");      // Saruman, Servant of Sauron: the Shadow player's [isengard] follower
					// Cast from the Hall: [rohan] support-area condition; it adds tokens only for a [rohan] possession
					// played from hand, and none is played here.
					put("cast", "18_94");

					put("savage", "1_151");       // Uruk Savage: strength 5, vitality 3; Damage +1 is its whole text
					put("runner", "1_178");       // Goblin Runner: strength 5
					put("troop", "1_177");        // Goblin Patrol Troop: strength 13, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GamlingsHornStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Gamling's Horn
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype:
		 * Game Text: Bearer must be a [rohan] Man or Merry.
		 * 	Skirmish: Discard a follower from play to make bearer strength +4. If bearer is Gamling, you may also spot
		 * 	another companion to make him or her strength +4 until the regroup phase.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("horn");

		assertEquals("Gamling's Horn", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MayBePlayedOnARohanManCompanionOrAllyOrOnTheRohanMerryWhoIsNoMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var guard = scn.GetFreepsCard("guard");
		var herdsman = scn.GetFreepsCard("herdsman");
		var merry = scn.GetFreepsCard("merry");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		var frodo = scn.GetRingBearer();
		// Two [rohan] companions (the Guard and Merry) and no Eowyn: Merry is no Man here, and only his title lets
		// him bear the Horn.
		scn.MoveCompanionsToTable(guard, merry, boromir, pippin);
		scn.MoveAlliesToTable(herdsman);
		scn.MoveCardsToHand(horn);

		scn.StartGame();
		assertTrue(scn.FreepsPlayAvailable(horn));

		scn.FreepsPlayCard(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, herdsman, merry));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(boromir, pippin, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());

		scn.FreepsChooseCard(merry);
		assertAttachedTo(horn, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void CannotBePlayedOnAManOfAnotherCultureOrAnotherHobbitUntilAShireMerryIsPlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var merry = scn.GetFreepsCard("merry2");
		var boromir = scn.GetFreepsCard("boromir");
		var pippin = scn.GetFreepsCard("pippin");
		scn.MoveCompanionsToTable(boromir, pippin);
		scn.MoveCardsToHand(horn, merry);

		scn.StartGame();
		// Frodo, Boromir and Pippin: no [rohan] Man and no Merry.
		assertInHand(horn);
		assertFalse(scn.FreepsPlayAvailable(horn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.FreepsPlayCard(merry);
		assertTrue(scn.FreepsPlayAvailable(horn));

		// The [shire] Merry is the one bearer: the Horn goes to him with nothing asked.
		scn.FreepsPlayCard(horn);
		assertAttachedTo(horn, merry);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void SkirmishAbilityDiscardsAFollowerOfEitherSideFromPlayToMakeBearerStrengthPlus4ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var guard = scn.GetFreepsCard("guard");
		var gamling = scn.GetFreepsCard("gamling");
		var boromir = scn.GetFreepsCard("boromir");
		var worker = scn.GetFreepsCard("worker");
		var worker2 = scn.GetFreepsCard("worker2");
		var army = scn.GetFreepsCard("army");
		var cast = scn.GetFreepsCard("cast");
		var saruman = scn.GetShadowCard("saruman");
		var savage = scn.GetShadowCard("savage");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(guard, gamling, boromir);
		// The Guard bears the Horn; Gamling is in play and bears nothing.
		scn.AttachCardsTo(guard, horn);
		// Followers in play: the Free Peoples player's in his support area and on Boromir, the Shadow player's in his.
		// A second Rohan Worker is in hand, and Cast from the Hall is a card in play that is no follower.
		scn.MoveCardsToSupportArea(worker, cast, saruman);
		scn.AttachCardsTo(boromir, army);
		scn.MoveCardsToHand(worker2);
		assertInZone(Zone.SUPPORT, worker, cast, saruman);
		assertAttachedTo(army, boromir);
		assertInHand(worker2);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{boromir, savage}, new PhysicalCardImpl[]{guard, troop});

		// Boromir's skirmish: the bearer is not in it.
		scn.FreepsResolveSkirmish(boromir);
		assertTrue(scn.FreepsActionAvailable(horn));
		assertEquals(6, scn.GetStrength(guard));
		assertEquals(6, scn.GetStrength(gamling));
		assertEquals(9, scn.GetStrength(boromir)); // 7 + 2 for Elendil's Army

		scn.FreepsUseCardAction(horn);
		assertTrue(scn.FreepsHasCardChoiceAvailable(worker, army, saruman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(worker2, cast, horn, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());

		scn.FreepsChooseCard(worker);
		assertInDiscard(worker);
		assertAttachedTo(army, boromir);
		assertInZone(Zone.SUPPORT, saruman, cast);
		assertInHand(worker2);
		assertEquals(10, scn.GetStrength(guard));
		// The bearer is no Gamling: nobody else is offered the +4.
		assertEquals(6, scn.GetStrength(gamling));
		assertEquals(9, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The next skirmish is another phase: the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(guard);
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithNoFollowerInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var guard = scn.GetFreepsCard("guard");
		var worker = scn.GetFreepsCard("worker");
		var cast = scn.GetFreepsCard("cast");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, horn);
		scn.MoveCardsToSupportArea(cast);
		scn.MoveCardsToHand(worker);
		assertInZone(Zone.SUPPORT, cast);
		assertInHand(worker);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, savage);
		scn.FreepsResolveSkirmish(guard);

		// A follower in hand and a condition in play: nothing to pay with.
		assertFalse(scn.FreepsActionAvailable(horn));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void OnGamlingAnotherCompanionMayAlsoBeMadeStrengthPlus4UntilTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var gamling = scn.GetFreepsCard("gamling");
		var guard = scn.GetFreepsCard("guard");
		var boromir = scn.GetFreepsCard("boromir");
		var herdsman = scn.GetFreepsCard("herdsman");
		var worker = scn.GetFreepsCard("worker");
		var frodo = scn.GetRingBearer();
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(gamling, guard, boromir);
		scn.MoveAlliesToTable(herdsman);
		scn.AttachCardsTo(gamling, horn);
		scn.MoveCardsToSupportArea(worker);
		assertAttachedTo(horn, gamling);
		assertInZone(Zone.SUPPORT, worker);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gamling, savage}, new PhysicalCardImpl[]{boromir, runner});

		scn.FreepsResolveSkirmish(gamling);
		assertEquals(6, scn.GetStrength(gamling));
		assertEquals(7, scn.GetStrength(boromir));

		// The one follower is discarded with nothing asked.
		scn.FreepsUseCardAction(horn);
		assertInDiscard(worker);
		assertEquals(10, scn.GetStrength(gamling));

		assertTrue(scn.FreepsDecisionAvailable("Would you like to spot another companion"));
		scn.FreepsChooseYes();
		// Another companion: Boromir, the Guard and Frodo; not Gamling himself, not an ally, not a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(boromir, guard, frodo));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gamling, herdsman, savage, runner));
		scn.FreepsChooseCard(boromir);

		assertEquals(11, scn.GetStrength(boromir));
		assertEquals(10, scn.GetStrength(gamling));
		assertEquals(6, scn.GetStrength(guard));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Boromir's own skirmish, a later phase: his +4 holds; Gamling's is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(boromir);
		assertEquals(11, scn.GetStrength(boromir));
		assertEquals(6, scn.GetStrength(gamling));

		// Boromir wins (11 against 5); at the regroup phase the +4 is gone.
		scn.PassCurrentPhaseActions();
		assertEquals(Phase.REGROUP, scn.GetCurrentPhase());
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void OnGamlingTheSecondPlus4MayBeDeclined() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var horn = scn.GetFreepsCard("horn");
		var gamling = scn.GetFreepsCard("gamling");
		var boromir = scn.GetFreepsCard("boromir");
		var worker = scn.GetFreepsCard("worker");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(gamling, boromir);
		scn.AttachCardsTo(gamling, horn);
		scn.MoveCardsToSupportArea(worker);
		assertAttachedTo(horn, gamling);
		assertInZone(Zone.SUPPORT, worker);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gamling, savage);
		scn.FreepsResolveSkirmish(gamling);

		scn.FreepsUseCardAction(horn);
		assertInDiscard(worker);
		assertTrue(scn.FreepsDecisionAvailable("Would you like to spot another companion"));
		scn.FreepsChooseNo();

		assertEquals(10, scn.GetStrength(gamling));
		assertEquals(7, scn.GetStrength(boromir));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
