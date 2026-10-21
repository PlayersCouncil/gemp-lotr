package com.gempukku.lotro.cards.official.set09;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_09_021_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dagger", "9_21");
					put("pippin", "1_306");     // Pippin, Friend to Frodo: [shire] Hobbit companion, vitality 4; his text is about [shire] tales
					put("trollslayer", "103_31"); // Pippin, Trollslayer: the [gondor] Pippin; a skirmish ability only
					put("merry", "1_302");      // Merry, Friend to Sam: a Hobbit companion who is no Pippin, vitality 4; a skirmish ability only
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; +3 only against a Nazgul
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: a second Elf companion; +2 only at sites 6 to 8
					put("golradir", "3_20");    // Golradir, Councilor of Imladris: Elf ally, home site 3; a skirmish ability only

					put("savage", "1_151");     // Uruk Savage: minion, vitality 3; damage +1 only
					put("runner", "1_178");     // Goblin Runner: minion, vitality 1 (exhausted); its trigger is on being played
					put("troop", "1_177");      // Goblin Patrol Troop: minion, vitality 3, no text
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void PippinsDaggerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 9
		 * Name: Pippin's Dagger
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be Pippin.<br><b>Skirmish:</b> Exert Pippin twice or discard this possession to wound a minion an Elf is skirmishing. Any Shadow player may remove (1) to prevent this.
		*/

		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");

		assertEquals("Pippin's Dagger", dagger.getBlueprint().getTitle());
		assertNull(dagger.getBlueprint().getSubtitle());
		assertTrue(dagger.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, dagger.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, dagger.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, dagger.getBlueprint().getCardType());
		assertTrue(dagger.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, dagger.getBlueprint().getTwilightCost());
		assertEquals(2, dagger.getBlueprint().getStrength());
	}

	@Test
	public void IsNotPlayableWithoutPippinAndGoesOnPippinOnceHeIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(merry);
		scn.MoveCardsToHand(dagger);
		assertInHand(dagger);

		scn.StartGame();

		// A Hobbit companion of Pippin's culture who is no Pippin, and the Ring-bearer: not playable.
		assertTrue(scn.AwaitingFellowshipPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(dagger));

		scn.MoveCompanionsToTable(pippin);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(dagger));
		scn.FreepsPlayCard(dagger);

		// Pippin is the one bearer it can go on: nothing is asked.
		assertAttachedTo(dagger, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void IsPlayedOnTheGondorPippin() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var merry = scn.GetFreepsCard("merry");
		scn.MoveCompanionsToTable(trollslayer, merry);
		scn.MoveCardsToHand(dagger);
		assertInHand(dagger);

		scn.StartGame();

		assertTrue(scn.FreepsPlayAvailable(dagger));
		scn.FreepsPlayCard(dagger);

		assertAttachedTo(dagger, trollslayer);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ExertingPippinTwiceWoundsAMinionAnElfIsSkirmishingWhenTheShadowPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var pippin = scn.GetFreepsCard("pippin");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var troop = scn.GetShadowCard("troop");
		scn.MoveCompanionsToTable(pippin, arwen, haldir);
		scn.AttachCardsTo(pippin, dagger);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner, troop);
		assertAttachedTo(dagger, pippin);
		scn.SkipToAssignments();
		// The Savage on Arwen, the Troop on Haldir; the Shadow player puts the Runner on Arwen too.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{arwen, savage}, new PhysicalCardImpl[]{haldir, troop});
		scn.ShadowAssignToMinions(arwen, runner);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		int twilight = scn.GetTwilight();
		assertTrue(twilight >= 1);
		assertTrue(scn.FreepsActionAvailable(dagger));

		scn.FreepsUseCardAction(dagger);
		// Both costs can be paid: the player is asked which.
		assertEquals(List.of("Exert Pippin twice", "Discard this possession"), scn.FreepsGetMultipleChoices());
		scn.FreepsChoose("Exert Pippin twice");
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertAttachedTo(dagger, pippin);

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// The two minions in Arwen's skirmish are offered; the Troop, assigned to an Elf whose skirmish waits, is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(savage, runner));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.FreepsGetChoiceMin());
		assertEquals(1, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCard(runner);

		// The exhausted Runner is wounded, and so killed; nothing else changes.
		assertInDiscard(runner);
		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void DiscardingTheDaggerIsStillPaidWhenTheShadowPlayerRemovesOneToPreventTheWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var pippin = scn.GetFreepsCard("pippin");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin, arwen);
		scn.AttachCardsTo(pippin, dagger);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertAttachedTo(dagger, pippin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		scn.SetTwilight(3);
		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsUseCardAction(dagger);
		scn.FreepsChoose("Discard this possession");
		assertInDiscard(dagger);
		assertEquals(0, scn.GetWoundsOn(pippin));

		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// (1) removed and the wound prevented; the dagger stays discarded.
		assertEquals(2, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInDiscard(dagger);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WhenPippinCannotExertTwiceTheDaggerIsDiscardedAndAnEmptyPoolCannotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var pippin = scn.GetFreepsCard("pippin");
		var merry = scn.GetFreepsCard("merry");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin, merry, arwen);
		scn.AttachCardsTo(pippin, dagger);

		scn.StartGame();
		// Vitality 4 with 2 wounds: Pippin can exert once, not twice.  Merry could exert twice, but is no Pippin.
		scn.AddWoundsToChar(pippin, 2);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		scn.MoveMinionsToTable(savage);
		assertAttachedTo(dagger, pippin);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		scn.SetTwilight(0);
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(savage));

		// Only the discard can be paid: nothing is asked, and with no twilight the Shadow player is not asked either.
		scn.FreepsUseCardAction(dagger);
		assertInDiscard(dagger);
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merry));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void TheGondorPippinPaysTheExertionsToo() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var trollslayer = scn.GetFreepsCard("trollslayer");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(trollslayer, arwen);
		scn.AttachCardsTo(trollslayer, dagger);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertAttachedTo(dagger, trollslayer);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(arwen, savage);
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(0, scn.GetWoundsOn(trollslayer));
		assertEquals(0, scn.GetWoundsOn(savage));

		scn.FreepsUseCardAction(dagger);
		assertTrue(scn.FreepsChoiceAvailable("Exert Pippin twice"));
		scn.FreepsChoose("Exert Pippin twice");
		scn.ShadowChooseNo();

		assertEquals(2, scn.GetWoundsOn(trollslayer));
		assertAttachedTo(dagger, trollslayer);
		assertEquals(1, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void WoundsAMinionAnElfAllyIsSkirmishing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var pippin = scn.GetFreepsCard("pippin");
		var golradir = scn.GetFreepsCard("golradir");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin);
		scn.AttachCardsTo(pippin, dagger);
		scn.MoveAlliesToTable(golradir);

		scn.StartGame();
		scn.SkipToSite(2);
		scn.MoveMinionsToTable(savage);
		assertAttachedTo(dagger, pippin);
		// The fellowship moves on to site 3, Golradir's home, where he may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(golradir, savage);
		scn.FreepsResolveSkirmish(golradir);

		assertEquals(0, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(pippin));

		scn.FreepsUseCardAction(dagger);
		scn.FreepsChoose("Exert Pippin twice");
		scn.ShadowChooseNo();

		// The one minion the Elf ally is skirmishing is wounded.
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void InASkirmishWithNoElfTheCostIsPaidForNothingAndNoPreventionIsAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var pippin = scn.GetFreepsCard("pippin");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(pippin, arwen);
		scn.AttachCardsTo(pippin, dagger);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		assertAttachedTo(dagger, pippin);
		scn.SkipToAssignments();
		// Pippin, no Elf, fights the Savage first; Arwen's skirmish with the Runner waits.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{pippin, savage}, new PhysicalCardImpl[]{arwen, runner});
		scn.FreepsResolveSkirmish(pippin);

		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInPlay(runner);
		int twilight = scn.GetTwilight();
		assertTrue(twilight >= 1);
		assertTrue(scn.FreepsActionAvailable(dagger));

		scn.FreepsUseCardAction(dagger);
		scn.FreepsChoose("Exert Pippin twice");

		// The cost is paid; no minion is skirmishing an Elf, so nobody is wounded and no prevention is asked.
		assertEquals(2, scn.GetWoundsOn(pippin));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertInPlay(runner);
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedInTheAssignmentPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dagger = scn.GetFreepsCard("dagger");
		var pippin = scn.GetFreepsCard("pippin");
		var arwen = scn.GetFreepsCard("arwen");
		var savage = scn.GetShadowCard("savage");
		scn.MoveCompanionsToTable(pippin, arwen);
		scn.AttachCardsTo(pippin, dagger);

		scn.StartGame();
		scn.MoveMinionsToTable(savage);
		assertAttachedTo(dagger, pippin);
		assertEquals(0, scn.GetWoundsOn(pippin));
		scn.SkipToPhase(Phase.ASSIGNMENT);

		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertFalse(scn.FreepsActionAvailable(dagger));
	}
}
