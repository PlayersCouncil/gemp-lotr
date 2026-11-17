package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_013_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("phial", "10_13");
					// Sam, Son of Hamfast: a Hobbit, strength 3, vitality 4; a fellowship ability, and a response if
					// Frodo dies
					put("sam", "1_311");
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4

					// Pippin, Trollslayer: a [gondor] Hobbit companion; to play, spot a [gondor] companion, so he is
					// cheated in; a skirmish ability used only on purpose
					put("pippin", "103_31");
					// Bounder: a [shire] Hobbit ally, home 2F; a skirmish ability used only on purpose
					put("bounder", "1_286");
					// Smeagol, Once Like Us: a [shire] companion who is no Hobbit; an assignment ability used only on purpose
					put("smeagol", "103_116");

					put("savage", "1_151");     // Uruk Savage: Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("party", "1_158");      // Uruk-hai Raiding Party: Uruk-hai, strength 9, vitality 3; damage +1 is its whole text
					// Footman of Dunland: [men] Man, strength 7, vitality 3; the site gains plains
					put("footman", "11_82");
					// Lurtz's Battle Cry: [isengard] Skirmish event, cost 1: make an Uruk-hai strength -1 and damage +1
					put("battlecry", "1_128");
					put("battlecry2", "1_128");
					// White Hand Butcher: [uruk-hai] Uruk-hai, strength 5, vitality 2; hunter 6 is its whole text, so it
					// has no damage +1 for Lurtz's Battle Cry to give (the control that the Battle Cry works)
					put("butcher", "17_122");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PhialofGaladrielStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Phial of Galadriel, Star-glass
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 0
		 * Type: Artifact
		 * Subtype: 
		 * Game Text: Bearer must be a Hobbit.<br><b>Skirmish:</b> Until the regroup phase, remove the game text (except card type and race) from a minion skirmishing bearer and make it unable to gain game text. Discard this artifact.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("phial");

		assertEquals("Phial of Galadriel", card.getBlueprint().getTitle());
		assertEquals("Star-glass", card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.ARTIFACT, card.getBlueprint().getCardType());
		assertNull(card.getBlueprint().getPossessionClasses());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void RemovesTheGameTextOfAMinionSkirmishingBearerAndIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, phial);
		assertAttachedTo(phial, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, party);
		scn.SkipToAssignments();
		// The Savage fights the bearer; the Raiding Party waits for Aragorn, whose skirmish has not begun.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{sam, savage}, new PhysicalCardImpl[]{aragorn, party});
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(party, Keyword.DAMAGE));
		assertAttachedTo(phial, sam);
		assertTrue(scn.FreepsActionAvailable(phial));
		scn.FreepsUseCardAction(phial);

		// The minion skirmishing the bearer has lost its damage +1 and is an Uruk-hai minion still; the minion
		// assigned to Aragorn keeps its own.
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertTrue(scn.IsType(savage, CardType.MINION));
		assertTrue(scn.IsRace(savage, Race.URUK_HAI));
		assertTrue(scn.HasKeyword(party, Keyword.DAMAGE));
		assertInDiscard(phial);

		// Sam's 3 loses to the Savage's 5 and is not overwhelmed: one wound, where damage +1 would have made two.
		assertEquals(0, scn.GetWoundsOn(sam));
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(sam));

		// Aragorn's 8 loses to the Raiding Party's 9, damage +1 and all: two wounds.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
	@Test
	public void PlaysOnAHobbitCompanionOrAllyOfAnyCultureAndOnNoOtherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var phial = scn.GetFreepsCard("phial");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(phial);
		assertInZone(Zone.HAND, phial);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(phial));
		scn.FreepsPlayCard(phial);

		// The Ring-bearer, a [gondor] Hobbit and a Hobbit ally; not a Man, nor a [shire] companion who is no Hobbit.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn, smeagol));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(phial, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void TheChosenOfTwoMinionsSkirmishingBearerCannotGainAKeywordAndHasItsTextBackAtTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var footman = scn.GetShadowCard("footman");
		var party = scn.GetShadowCard("party");
		var battlecry = scn.GetShadowCard("battlecry");
		var battlecry2 = scn.GetShadowCard("battlecry2");
		var butcher = scn.GetShadowCard("butcher");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, phial);
		scn.MoveCardsToHand(battlecry, battlecry2);
		assertAttachedTo(phial, sam);
		assertInZone(Zone.HAND, battlecry, battlecry2);

		scn.StartGame();
		scn.SetTwilight(10);
		scn.MoveMinionsToTable(savage, footman, party, butcher);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, savage},
				new PhysicalCardImpl[]{aragorn, party}
		);
		// The minions left over: the Shadow player puts the Footman on Sam as well, and the Butcher on Aragorn.
		scn.ShadowAssignToMinions(
				new PhysicalCardImpl[]{sam, footman},
				new PhysicalCardImpl[]{aragorn, butcher}
		);
		scn.FreepsResolveSkirmish(sam);

		assertTrue(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(butcher, Keyword.DAMAGE));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(footman));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		scn.FreepsUseCardAction(phial);

		// Both minions skirmishing the bearer; not the Raiding Party, which waits for Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, footman));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(party, sam));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(savage);

		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(party, Keyword.DAMAGE));
		assertInDiscard(phial);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// The Shadow player makes the Savage strength -1 and damage +1: the strength is no game text and is
		// applied; the keyword is game text, and the Savage cannot gain it.
		scn.ShadowPlayCard(battlecry);
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, party));
		scn.ShadowChooseCard(savage);
		assertEquals(4, scn.GetStrength(savage));      // 5 - 1
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// The control: the same Battle Cry does give damage +1 to an Uruk-hai that has none, the Butcher.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		scn.ShadowPlayCard(battlecry2);
		scn.ShadowChooseCard(butcher);
		assertEquals(4, scn.GetStrength(butcher));     // 5 - 1
		assertTrue(scn.HasKeyword(butcher, Keyword.DAMAGE));
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Sam (3) is overwhelmed by the two (11) and killed.
		scn.PassSkirmishActions();
		assertInZone(Zone.DEAD, sam);

		// In Aragorn's skirmish, a later skirmish phase: the Savage is still without its text.
		scn.FreepsResolveSkirmish(aragorn);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.HasKeyword(savage, Keyword.DAMAGE));

		scn.PassSkirmishActions();
		// At the regroup phase its text is back.
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.HasKeyword(savage, Keyword.DAMAGE));
	}

	@Test
	public void UsedInAnotherCompanionsSkirmishItRemovesNoTextAndIsDiscardedAndIsNoAssignmentAction() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var phial = scn.GetFreepsCard("phial");
		var sam = scn.GetFreepsCard("sam");
		var aragorn = scn.GetFreepsCard("aragorn");
		var savage = scn.GetShadowCard("savage");
		var party = scn.GetShadowCard("party");
		scn.MoveCompanionsToTable(sam, aragorn);
		scn.AttachCardsTo(sam, phial);
		assertAttachedTo(phial, sam);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, party);
		scn.SkipToPhase(Phase.ASSIGNMENT);

		// The assignment phase, next to the skirmish phase: not offered.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(phial));

		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{sam, savage},
				new PhysicalCardImpl[]{aragorn, party}
		);
		scn.FreepsResolveSkirmish(aragorn);

		assertTrue(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(party, Keyword.DAMAGE));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		// Its own words discard it, so it is offered with no minion skirmishing the bearer.
		assertTrue(scn.FreepsActionAvailable(phial));
		scn.FreepsUseCardAction(phial);

		// The Raiding Party fights Aragorn, who is not the bearer; the Savage is assigned to the bearer, whose
		// skirmish has not begun.  Neither loses its text.
		assertTrue(scn.HasKeyword(party, Keyword.DAMAGE));
		assertTrue(scn.HasKeyword(savage, Keyword.DAMAGE));
		assertInDiscard(phial);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
