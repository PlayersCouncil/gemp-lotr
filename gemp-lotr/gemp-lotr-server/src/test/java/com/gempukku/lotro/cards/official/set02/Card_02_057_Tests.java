package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_057_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("cry", "2_57");
					// The Balrog, The Terror of Khazad-dum: strength 17, vitality 5, damage +1.  The rest of its text
					// acts only at an underground site, and these skirmishes are at Ettenmoors.
					put("balrog", "12_79");
					// Whip of Many Thongs: "Bearer must be The Balrog", strength +1.  Its skirmish ability is never used.
					put("whip", "2_74");
					// The Balrog's Sword: another hand weapon The Balrog may bear, strength +3 and damage +1.
					put("sword", "2_50");
					// Goblin Runner: strength 5, vitality 1; the minion of the other skirmish.
					put("runner", "1_178");

					// Aragorn, Ranger of the North: strength 8, vitality 4.  His maneuver ability is never used.
					put("aragorn", "1_89");
					// Gimli, Son of Gloin: strength 6, vitality 3; his skirmish ability is never used.
					put("gimli", "1_13");
					// Thrarin, Dwarven Smith: a Dwarf ally, strength 4, vitality 2.  "Maneuver: Exert Thrarin to allow
					// him to participate in archery fire and skirmishes until the regroup phase."
					put("thrarin", "1_27");
					// Merry, From O'er the Brandywine: a Hobbit, strength 3, vitality 4; +2 bearing a weapon, and he bears none.
					put("merry", "1_303");
					// Hobbit Stealth: "Skirmish: At sites 1 to 5, cancel a skirmish involving a Hobbit."
					put("stealth", "1_298");
					// Smeagol, Slippery Sneak: a companion; "Skirmish: Add threats equal to the total vitality of the
					// minions Smeagol is skirmishing to discard Smeagol."  His regroup ability is never used.
					put("smeagol", "8_27");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void FinalCryStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Final Cry
		 * Unique: False
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Response
		 * Game Text: <b>Response:</b> If a skirmish that involved The Balrog bearing Whip of Many Thongs is about to end, wound a companion in that skirmish twice.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("cry");

		assertEquals("Final Cry", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayedAsTheBalrogsSkirmishIsAboutToEndItWoundsTheCompanionInItTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var balrog = scn.GetShadowCard("balrog");
		var whip = scn.GetShadowCard("whip");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(cry);

		assertInZone(Zone.HAND, cry);
		scn.StartGame();
		scn.MoveMinionsToTable(balrog, runner);
		scn.AttachCardsTo(balrog, whip);
		assertAttachedTo(whip, balrog);
		// So that Aragorn wins and takes no wound of the skirmish's own: 8 + 15 against 17 + 1, and not double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 15));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, balrog}, new PhysicalCardImpl[]{gimli, runner});
		scn.SetTwilight(3);

		// The other skirmish first: it did not involve The Balrog, who waits in his own.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		assertInZone(Zone.DISCARD, runner);
		// Nothing was offered as it ended: the game has gone straight on to the choice of the next skirmish, where an
		// offer to the Shadow player would have come first.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertInZone(Zone.HAND, cry);
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(23, scn.GetStrength(aragorn));
		assertEquals(18, scn.GetStrength(balrog));
		// While the skirmish is being fought the event is not offered.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(cry));
		scn.ShadowPass();

		// Resolved, The Balrog has the loser's wound: the skirmish is about to end.
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowPlayAvailable(cry));
		scn.ShadowPlayCard(cry);

		// Two wounds on the companion of that skirmish; none on Gimli, whose skirmish is over, nor on Frodo, who
		// was in none.
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertInDiscard(cry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTheBalrogBearingAnotherWeaponAndNoWhipItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var balrog = scn.GetShadowCard("balrog");
		var sword = scn.GetShadowCard("sword");
		var runner = scn.GetShadowCard("runner");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.MoveCardsToHand(cry);

		assertInZone(Zone.HAND, cry);
		scn.StartGame();
		scn.MoveMinionsToTable(balrog, runner);
		scn.AttachCardsTo(balrog, sword);
		assertAttachedTo(sword, balrog);
		// 8 + 15 against 17 + 3: Aragorn wins.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 15));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, balrog}, new PhysicalCardImpl[]{gimli, runner});
		scn.SetTwilight(3);

		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(20, scn.GetStrength(balrog));
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(balrog));
		assertAttachedTo(sword, balrog);
		// The skirmish ended with no offer: the game has gone straight on to the choice of the next one, where an
		// offer to the Shadow player would have come first.
		assertTrue(scn.FreepsDecisionAvailable("Choose next skirmish to resolve"));
		assertInZone(Zone.HAND, cry);
		assertEquals(0, scn.GetWoundsOn(aragorn));
	}

	@Test
	public void AnExhaustedCompanionInThatSkirmishIsWoundedAndKilled() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var balrog = scn.GetShadowCard("balrog");
		var whip = scn.GetShadowCard("whip");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.AddWoundsToChar(aragorn, 3);
		scn.MoveCardsToHand(cry);

		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.AttachCardsTo(balrog, whip);
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 15));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, balrog);
		scn.SetTwilight(3);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// Aragorn won, and has 1 vitality left: he cannot exert, but he can be wounded.
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(balrog));
		assertTrue(scn.ShadowPlayAvailable(cry));
		scn.ShadowPlayCard(cry);

		assertInZone(Zone.DEAD, aragorn);
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(cry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithAnAllySkirmishingTheBalrogItCanBePlayedAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var balrog = scn.GetShadowCard("balrog");
		var whip = scn.GetShadowCard("whip");
		var thrarin = scn.GetFreepsCard("thrarin");
		var frodo = scn.GetRingBearer();
		scn.MoveAlliesToTable(thrarin);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.AttachCardsTo(balrog, whip);
		// 4 + 20 against 17 + 1: Thrarin wins.
		scn.ApplyAdHocModifier(new StrengthModifier(null, thrarin, null, 20));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsUseCardAction(thrarin);
		assertEquals(1, scn.GetWoundsOn(thrarin));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(thrarin, balrog);
		scn.SetTwilight(3);

		scn.FreepsResolveSkirmish(thrarin);
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(balrog));
		assertEquals(1, scn.GetWoundsOn(thrarin));
		// An event's target is no requirement: it is offered, and played for nothing.  The ally is no companion,
		// and Frodo is in no skirmish.
		assertTrue(scn.ShadowPlayAvailable(cry));
		scn.ShadowPlayCard(cry);

		assertEquals(1, scn.GetWoundsOn(thrarin));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(cry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBalrogKilledByHisLosersWoundBearsNoWhipAsTheSkirmishIsAboutToEndAndItIsNotOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var balrog = scn.GetShadowCard("balrog");
		var whip = scn.GetShadowCard("whip");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(cry);

		assertInZone(Zone.HAND, cry);
		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.AttachCardsTo(balrog, whip);
		scn.AddWoundsToChar(balrog, 4);
		assertAttachedTo(whip, balrog);
		assertEquals(4, scn.GetWoundsOn(balrog));
		// 8 + 15 against 17 + 1: Aragorn wins, and not by double.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 15));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, balrog);
		scn.SetTwilight(3);

		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();

		// The loser's wound is his fifth: The Balrog is killed and the Whip goes with him, before the skirmish is
		// about to end (Q-0332: B; the skirmish procedure's step 5 comes before its step 6).  The game has gone on
		// to the regroup phase with no offer.
		assertInDiscard(balrog, whip);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertInZone(Zone.HAND, cry);
		assertEquals(3, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheCompanionKilledByLosingItIsOfferedAndWoundsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var balrog = scn.GetShadowCard("balrog");
		var whip = scn.GetShadowCard("whip");
		var aragorn = scn.GetFreepsCard("aragorn");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.AttachCardsTo(balrog, whip);
		assertAttachedTo(whip, balrog);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, balrog);
		scn.SetTwilight(3);

		scn.FreepsResolveSkirmish(aragorn);
		// 17 + 1 against 8: at least double.
		assertEquals(18, scn.GetStrength(balrog));
		assertEquals(8, scn.GetStrength(aragorn));
		scn.PassSkirmishActions();

		// Aragorn is overwhelmed and killed (step 5); The Balrog, bearing the Whip, is still in the skirmish as it
		// is about to end (step 6).
		assertInZone(Zone.DEAD, aragorn);
		assertAttachedTo(whip, balrog);
		assertTrue(scn.ShadowPlayAvailable(cry));
		scn.ShadowPlayCard(cry);

		// No companion is left in that skirmish: Frodo, in none, is not wounded.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertInDiscard(cry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ACanceledSkirmishIsAboutToEndAndTheCompanionInItIsWoundedTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var balrog = scn.GetShadowCard("balrog");
		var whip = scn.GetShadowCard("whip");
		var merry = scn.GetFreepsCard("merry");
		var stealth = scn.GetFreepsCard("stealth");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(cry, stealth);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.AttachCardsTo(balrog, whip);
		assertAttachedTo(whip, balrog);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(merry, balrog);
		scn.SetTwilight(3);

		// At Ettenmoors, site 2: Hobbit Stealth cancels the skirmish (step 2a), which goes straight to step 6.
		scn.FreepsResolveSkirmish(merry);
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsPlayCard(stealth);

		// Nobody won or lost: no wound was placed.
		assertEquals(0, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(balrog));
		assertAttachedTo(whip, balrog);
		assertTrue(scn.ShadowPlayAvailable(cry));
		scn.ShadowPlayCard(cry);

		// Merry is still in that skirmish as it is about to end (he is unassigned at step 7): the two wounds are his.
		assertEquals(2, scn.GetWoundsOn(merry));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(cry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ASkirmishWhoseCompanionIsRemovedFromItIsAboutToEndAndItIsOffered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var cry = scn.GetShadowCard("cry");
		var balrog = scn.GetShadowCard("balrog");
		var whip = scn.GetShadowCard("whip");
		var smeagol = scn.GetFreepsCard("smeagol");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(smeagol);
		scn.MoveCardsToHand(cry);

		scn.StartGame();
		scn.MoveMinionsToTable(balrog);
		scn.AttachCardsTo(balrog, whip);
		// The Balrog has 1 vitality left, so that Smeagol's ability costs 1 threat.
		scn.AddWoundsToChar(balrog, 4);
		assertAttachedTo(whip, balrog);
		assertEquals(4, scn.GetWoundsOn(balrog));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(smeagol, balrog);
		scn.SetTwilight(3);

		// Smeagol discards himself: the last character of his side is removed (step 2c), the Free Peoples side has
		// lost, and a side already removed takes no loss effects (step 5a).
		scn.FreepsResolveSkirmish(smeagol);
		scn.FreepsUseCardAction(smeagol);
		assertInDiscard(smeagol);
		assertEquals(1, scn.GetThreats());

		assertEquals(4, scn.GetWoundsOn(balrog));
		assertAttachedTo(whip, balrog);
		assertTrue(scn.ShadowPlayAvailable(cry));
		scn.ShadowPlayCard(cry);

		// No companion is left in that skirmish, and Frodo is in none.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertInDiscard(cry);
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
