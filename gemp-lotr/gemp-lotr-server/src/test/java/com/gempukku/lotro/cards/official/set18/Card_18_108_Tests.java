package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_18_108_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ale", "18_108");
					put("song", "8_112");       // Song of the Shire: [shire] support-area condition that holds [shire] tokens; abilities used only on purpose

					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit, strength 3, +2 while he bears a weapon
					put("sword", "1_299");      // Hobbit Sword: hand weapon, strength +2, on a Hobbit
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit, strength 3 (cheated in); an assignment ability only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3 (cheated in); a skirmish ability used only on purpose
					put("bounder", "1_286");    // Bounder: a [shire] Hobbit ally, strength 2; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: pays for One Whom Men Would Follow; a maneuver ability used only on purpose
					put("follow", "1_109");     // One Whom Men Would Follow: [gondor] maneuver event, exert Aragorn so that an ally is +2 and takes part in skirmishes

					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
					put("runner2", "1_178");
					put("pursuer", "4_190");    // Uruk Pursuer: fierce [isengard] Uruk-hai, strength 5, vitality 3; nothing else that reaches these tests
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	// The Fellowship path with Redhorn Pass 13_194 ("Culture tokens cannot be added, removed, or reinforced") in
	// place of a site no test reaches; the test lays it as the site after the first.
	protected VirtualTableScenario GetRedhornPassScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("ale", "18_108");
					put("merry", "1_303");      // Merry, From O'er the Brandywine: [shire] Hobbit, strength 3, +2 while he bears a weapon
					put("sword", "1_299");      // Hobbit Sword: hand weapon, strength +2, on a Hobbit
					put("runner1", "1_178");    // Goblin Runner: [moria] Orc, strength 5, vitality 1
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.FellowshipSites);
					remove("site9");
					put("redhorn", "13_194");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void GoldenPerchAleStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Golden Perch Ale
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Support Area
		 * Game Text: Each time a [shire] companion wins a skirmish, you may add a [shire] token here.<br><b>Fellowship:</b> Remove a [shire] token here to remove a burden.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("ale");

		assertEquals("Golden Perch Ale", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void EachTimeAShireCompanionOfAnyRaceWinsASkirmishATokenMayBeAddedHere() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ale = scn.GetFreepsCard("ale");
		var song = scn.GetFreepsCard("song");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var smeagol = scn.GetFreepsCard("smeagol");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(merry, smeagol);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToSupportArea(ale, song);
		// Another [shire] card holding a [shire] token: the token goes on this card and no other.
		scn.AddTokensToCard(song, 1);
		assertAttachedTo(sword, merry);
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(0, scn.GetCultureTokensOn(ale));

		scn.StartGame();
		// Smeagol is made strong enough to win; only the board needs it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, smeagol, null, 5));
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{merry, runner1}, new PhysicalCardImpl[]{smeagol, runner2});

		// A Hobbit: 3 + 2 for the Sword + 2 while he bears a weapon, against 5.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(7, scn.GetStrength(merry));
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ale));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(ale));
		assertEquals(1, scn.GetCultureTokensOn(song));

		// A [shire] companion who is no Hobbit: 3 + 5 against 5.
		scn.FreepsResolveSkirmish(smeagol);
		assertEquals(8, scn.GetStrength(smeagol));
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ale));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(ale));
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNoToken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ale = scn.GetFreepsCard("ale");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToSupportArea(ale);
		assertAttachedTo(sword, merry);
		assertInZone(Zone.SUPPORT, ale);
		assertEquals(0, scn.GetCultureTokensOn(ale));

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();

		assertInDiscard(runner1);
		assertEquals(0, scn.GetWoundsOn(merry));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ale));
		scn.FreepsDeclineOptionalTrigger();
		assertEquals(0, scn.GetCultureTokensOn(ale));
		assertInZone(Zone.SUPPORT, ale);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WinningAgainInTheFierceRoundItIsOfferedAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ale = scn.GetFreepsCard("ale");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var pursuer = scn.GetShadowCard("pursuer");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToSupportArea(ale);
		assertAttachedTo(sword, merry);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, pursuer);
		scn.FreepsResolveSkirmish(merry);
		// 7 against 5.
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ale));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(1, scn.GetCultureTokensOn(ale));

		// The fierce round: Merry against the Pursuer again, and the same win offers again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(merry, pursuer);
		scn.FreepsResolveSkirmish(merry);
		scn.PassSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(pursuer));
		assertTrue(scn.FreepsHasOptionalTriggerAvailable(ale));
		scn.FreepsAcceptOptionalTrigger();
		assertEquals(2, scn.GetCultureTokensOn(ale));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AGondorHobbitCompanionOrAShireAllyWinningIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ale = scn.GetFreepsCard("ale");
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var aragorn = scn.GetFreepsCard("aragorn");
		var follow = scn.GetFreepsCard("follow");
		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToSupportArea(ale);
		assertEquals(0, scn.GetCultureTokensOn(ale));

		scn.StartGame();
		// Each is made strong enough to win; only the board needs it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, pippin, null, 5));
		scn.ApplyAdHocModifier(new StrengthModifier(null, bounder, null, 4));
		scn.MoveCardsToHand(follow);
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToPhase(Phase.MANEUVER);
		// Bounder is made to take part in skirmishes.
		assertEquals(0, scn.GetWoundsOn(aragorn));
		scn.FreepsPlayCard(follow);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, runner1}, new PhysicalCardImpl[]{bounder, runner2});

		// A Hobbit companion of the [gondor] culture: 3 + 5 against 5.
		scn.FreepsResolveSkirmish(pippin);
		assertEquals(8, scn.GetStrength(pippin));
		scn.PassSkirmishActions();
		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ale));

		// A [shire] Hobbit who is an ally: 2 + 2 for the event + 4 against 5.
		scn.FreepsResolveSkirmish(bounder);
		assertEquals(8, scn.GetStrength(bounder));
		scn.PassSkirmishActions();
		assertInDiscard(runner2);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ale));
		assertEquals(0, scn.GetCultureTokensOn(ale));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void LosingASkirmishTheShireCompanionIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ale = scn.GetFreepsCard("ale");
		var merry = scn.GetFreepsCard("merry");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToSupportArea(ale);
		assertEquals(0, scn.GetCultureTokensOn(ale));

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);
		// 3 against 5: he loses and lives (5 is less than 6).
		assertEquals(3, scn.GetStrength(merry));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(merry));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ale));
		assertEquals(0, scn.GetCultureTokensOn(ale));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AtRedhornPassWhereNoTokenCanBeAddedTheOfferIsNotMade() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetRedhornPassScenario();

		var ale = scn.GetFreepsCard("ale");
		var merry = scn.GetFreepsCard("merry");
		var sword = scn.GetFreepsCard("sword");
		var runner1 = scn.GetShadowCard("runner1");
		scn.MoveCompanionsToTable(merry);
		scn.AttachCardsTo(merry, sword);
		scn.MoveCardsToSupportArea(ale);
		assertAttachedTo(sword, merry);

		scn.StartGame();
		scn.MoveCardToAdventurePath(scn.GetShadowSite("redhorn"));
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		assertEquals("Redhorn Pass", scn.GetCurrentSite().getBlueprint().getTitle());
		scn.FreepsAssignToMinions(merry, runner1);
		scn.FreepsResolveSkirmish(merry);
		// 7 against 5: Merry wins, as he does where the token is offered.
		scn.PassSkirmishActions();

		assertInDiscard(runner1);
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(ale));
		assertEquals(0, scn.GetCultureTokensOn(ale));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void FellowshipAbilityRemovesATokenHereToRemoveABurden() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ale = scn.GetFreepsCard("ale");
		var song = scn.GetFreepsCard("song");
		scn.MoveCardsToSupportArea(ale, song);

		scn.StartGame();
		scn.AddTokensToCard(ale, 1);
		scn.AddTokensToCard(song, 1);
		scn.AddBurdens(2);
		// The tokens and the burdens were laid under a decision already sent: ask it again.
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetCultureTokensOn(ale));
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(ale));

		scn.FreepsUseCardAction(ale);
		assertEquals(0, scn.GetCultureTokensOn(ale));
		// The token is this card's own: the other [shire] card keeps its token.
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ale));
	}

	@Test
	public void WithNoTokenHereItIsNotOfferedThoughAnotherShireCardHoldsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ale = scn.GetFreepsCard("ale");
		var song = scn.GetFreepsCard("song");
		scn.MoveCardsToSupportArea(ale, song);

		scn.StartGame();
		scn.AddTokensToCard(song, 1);
		scn.AddBurdens(1);
		scn.FreepsRefreshDecision();

		assertEquals(0, scn.GetCultureTokensOn(ale));
		assertEquals(1, scn.GetCultureTokensOn(song));
		assertEquals(1, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsActionAvailable(ale));
	}

	@Test
	public void WithNoBurdenItIsStillOfferedAndTheTokenIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var ale = scn.GetFreepsCard("ale");
		scn.MoveCardsToSupportArea(ale);

		scn.StartGame();
		scn.AddTokensToCard(ale, 1);
		scn.FreepsRefreshDecision();

		assertEquals(1, scn.GetCultureTokensOn(ale));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.FreepsActionAvailable(ale));

		scn.FreepsUseCardAction(ale);
		assertEquals(0, scn.GetCultureTokensOn(ale));
		assertEquals(0, scn.GetBurdens());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
