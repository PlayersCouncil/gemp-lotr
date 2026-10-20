package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_132_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("fury", "12_132");
					put("fury2", "12_132");
					// Merry, Learned Guide: "Skirmish: Exert the Ring-bearer to discard Merry. Any Shadow player may
					// remove (2) to prevent this."
					put("merry", "4_310");
					put("sam", "1_311");        // Sam, Son of Hamfast: [shire] Hobbit, strength 3, vitality 4
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, strength 3
					put("pippin", "103_31");    // Pippin, Trollslayer: a Hobbit companion who is [gondor], strength 3
					put("bounder", "1_286");    // Bounder: [shire] Hobbit ally whose home is site 2 of this path, strength 2
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8

					put("savage", "1_151");     // Uruk Savage: strength 5, vitality 3, damage +1
					put("pursuer", "4_190");    // Uruk Pursuer: strength 5, vitality 3, fierce; its texts are about unbound Hobbits
					put("runner", "1_178");     // Goblin Runner: strength 5, vitality 1
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void SuddenFuryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Sudden Fury
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 2
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: Each time a [shire] companion loses a skirmish, add a [shire] token here.
		 * <b>Skirmish:</b> Remove a [shire] token from here to make a [shire] companion strength +1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("fury");

		assertEquals("Sudden Fury", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeAShireCompanionLosesASkirmishEachCopyAddsATokenToItself() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var fury2 = scn.GetFreepsCard("fury2");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(fury, fury2);
		scn.MoveCompanionsToTable(sam);

		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, pursuer);
		scn.FreepsResolveSkirmish(sam);

		assertEquals(0, scn.GetCultureTokensOn(fury));
		assertEquals(0, scn.GetCultureTokensOn(fury2));

		// Sam (3) loses to the Pursuer (5).  His wound for losing and the two conditions' triggers come at once.
		scn.PassSkirmishActions();
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetCultureTokensOn(fury));
		assertEquals(1, scn.GetCultureTokensOn(fury2));

		// The Pursuer is fierce: the same companion loses to it again, and each copy adds a second token.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(sam, pursuer);
		scn.FreepsResolveSkirmish(sam);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();

		assertEquals(2, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetCultureTokensOn(fury));
		assertEquals(2, scn.GetCultureTokensOn(fury2));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ATokenIsAddedForAShireCompanionOfNoRaceAndNotForAGondorHobbitOrAShireAlly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToSupportArea(fury);
		scn.MoveCompanionsToTable(smeagol, pippin);
		scn.MoveAlliesToTable(bounder);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, runner2);
		// The fellowship has moved to site 2, the Bounder's home, where he may be assigned.
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, savage}, new PhysicalCardImpl[]{bounder, runner2},
				new PhysicalCardImpl[]{smeagol, runner});

		// A Hobbit companion who is [gondor] loses (3 against 5; two wounds, the Savage being damage +1): no token.
		scn.FreepsResolveSkirmish(pippin);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetCultureTokensOn(fury));

		// A [shire] Hobbit who is an ally loses (2 against 5, overwhelmed): no token.
		scn.FreepsResolveSkirmish(bounder);
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, bounder);
		assertEquals(0, scn.GetCultureTokensOn(fury));

		// A [shire] companion who is no Hobbit loses (3 against 5): a token.
		scn.FreepsResolveSkirmish(smeagol);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetCultureTokensOn(fury));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SkirmishActionRemovesATokenFromHereToMakeAShireCompanionStrengthPlusOneForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fury = scn.GetFreepsCard("fury");
		var fury2 = scn.GetFreepsCard("fury2");
		var sam = scn.GetFreepsCard("sam");
		var smeagol = scn.GetFreepsCard("smeagol");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCardsToSupportArea(fury, fury2);
		scn.MoveCompanionsToTable(sam, smeagol, pippin);
		scn.MoveAlliesToTable(bounder);
		scn.AddTokensToCard(fury, 2);
		scn.AddTokensToCard(fury2, 1);

		var runner = scn.GetShadowCard("runner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(runner, savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, runner}, new PhysicalCardImpl[]{smeagol, savage});
		scn.FreepsResolveSkirmish(sam);

		// Frodo is 3 and The Ruling Ring makes him 4.
		assertEquals(4, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(smeagol));
		assertTrue(scn.FreepsActionAvailable(fury));

		scn.FreepsUseCardAction(fury);
		assertEquals(1, scn.GetCultureTokensOn(fury));
		assertEquals(1, scn.GetCultureTokensOn(fury2));

		// Any [shire] companion, in the skirmish or out of it, Hobbit or not; not a Hobbit companion who is [gondor],
		// nor a [shire] Hobbit who is an ally.
		assertTrue(scn.FreepsHasCardChoiceAvailable(sam, frodo, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, bounder));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(frodo);
		assertEquals(5, scn.GetStrength(frodo));
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(3, scn.GetStrength(smeagol));

		scn.ShadowPass();
		scn.FreepsUseCardAction(fury);
		scn.FreepsChooseCard(sam);
		assertEquals(0, scn.GetCultureTokensOn(fury));
		assertEquals(1, scn.GetCultureTokensOn(fury2));
		assertEquals(4, scn.GetStrength(sam));
		assertEquals(5, scn.GetStrength(frodo));

		// With no token left here this copy has nothing to pay with; the other copy's token is its own.
		scn.ShadowPass();
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(fury));
		assertTrue(scn.FreepsActionAvailable(fury2));

		// Sam (4) still loses to the Runner (5).  In the next skirmish of the turn both bonuses are gone.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		scn.FreepsResolveSkirmish(smeagol);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(3, scn.GetStrength(sam));
		assertEquals(4, scn.GetStrength(frodo));
	}

	@Test
	public void AShireCompanionWhoWinsHisSkirmishAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCardsToSupportArea(fury);
		scn.MoveCompanionsToTable(sam);
		scn.AddTokensToCard(fury, 4);

		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		// Three tokens make Sam 6 against the Runner's 5.
		scn.FreepsUseCardAction(fury);
		scn.FreepsChooseCard(sam);
		scn.ShadowPass();
		scn.FreepsUseCardAction(fury);
		scn.FreepsChooseCard(sam);
		scn.ShadowPass();
		scn.FreepsUseCardAction(fury);
		scn.FreepsChooseCard(sam);
		assertEquals(6, scn.GetStrength(sam));
		assertEquals(1, scn.GetCultureTokensOn(fury));

		scn.PassSkirmishActions();

		// The Runner lost and is dead; the skirmish a [shire] companion won adds nothing here.
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetCultureTokensOn(fury));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	/**
	 * Legacy Ruling #2 decided that Sudden Fury (and other such cards that care about skirmish losers)
	 * does indeed trigger when the final character is removed from a skirmish.  Characters removed before
	 * the final removed one remain counting as "neither winning nor losing".
	 *
	 * <a href="https://wiki.lotrtcgpc.net/wiki/Legacy_Ruling_2">Legacy Ruling #2 on the wiki</a>
	 */
	@Test
	public void SuddenFuryAddsATokenWhenAHobbitIsTheLastFreepsSkirmisherAndIsRemoved() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var fury = scn.GetFreepsCard("fury");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCardsToSupportArea(fury);
		scn.MoveCompanionsToTable(merry);

		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, savage);
		scn.FreepsResolveSkirmish(merry);

		assertEquals(0, scn.GetCultureTokensOn(fury));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// Merry's own ability: the Ring-bearer exerts, and Merry is discarded out of his skirmish unless the Shadow
		// player pays (2), which he declines.
		scn.FreepsUseCardAction(merry);
		assertEquals(1, scn.GetWoundsOn(frodo));
		scn.ShadowChooseNo();
		assertInZone(Zone.DISCARD, merry);

		// Nobody is left on his side: the skirmish is over and he has lost it.  His side already removed, there
		// are no wounds for losing to resolve (step 5A), so the trigger has nothing to be ordered beside (engine
		// pass 54).
		assertEquals(1, scn.GetCultureTokensOn(fury));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void SuddenFuryDoesNotAddTokenWhenHobbitRemovedBeforeSkirmishBegins() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var fury = scn.GetFreepsCard("fury");
		var merry = scn.GetFreepsCard("merry");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCardsToSupportArea(fury);
		scn.MoveCompanionsToTable(merry, aragorn);

		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, savage}, new PhysicalCardImpl[]{aragorn, runner});

		// Aragorn's skirmish is fought first.  During it Merry, assigned to the Savage, has himself discarded.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.IsCharAssignedAgainst(merry, savage));
		scn.FreepsUseCardAction(merry);
		scn.ShadowChooseNo();
		assertInZone(Zone.DISCARD, merry);
		assertEquals(0, scn.GetCultureTokensOn(fury));

		// Aragorn (8) beats the Runner (5).  Merry's skirmish is never fought, so he lost none.
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetCultureTokensOn(fury));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
