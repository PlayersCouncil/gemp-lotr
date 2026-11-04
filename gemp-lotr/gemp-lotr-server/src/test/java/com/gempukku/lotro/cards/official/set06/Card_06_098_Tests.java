package com.gempukku.lotro.cards.official.set06;

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

public class Card_06_098_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "6_98");
					put("soldier", "1_271");     // Orc Soldier: [sauron] Orc, strength 7, vitality 2; a skirmish ability used only on purpose
					put("soldier2", "1_271");    // a second Orc Soldier, bearing nothing
					put("lieutenant", "1_280");  // Tower Lieutenant: [sauron] Orc; his text answers only his own win
					put("pillager", "10_92");    // Mordor Pillager: a fierce [sauron] Orc, strength 9; a Shadow ability used only on purpose
					put("sentry", "10_84");      // Cirith Ungol Sentry: a [sauron] Uruk-hai, no Orc; Damage +1 and nothing else
					put("runner", "1_178");      // Goblin Runner: a [moria] Orc; its text answers only its own play
					put("guard", "5_83");        // Household Guard: [rohan] Man companion, strength 6, vitality 3, Valiant and no other text
					put("guard2", "5_83");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BanneroftheEyeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Banner of the Eye
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 1
		 * Game Text: Bearer must be a [sauron] Orc.<br>Each time bearer wins a skirmish, you may add (1) for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("banner");

		assertEquals("Banner of the Eye", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnASauronOrcAndNotOnASauronUrukHaiOrAMoriaOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var lieutenant = scn.GetShadowCard("lieutenant");
		var sentry = scn.GetShadowCard("sentry");
		var runner = scn.GetShadowCard("runner");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.MoveMinionsToTable(soldier, lieutenant, sentry, runner);
		scn.SetShadowHand(banner);
		assertInHand(banner);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(banner));
		scn.ShadowPlayCard(banner);

		assertTrue(scn.ShadowHasCardChoiceAvailable(soldier, lieutenant));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lieutenant);

		assertAttachedTo(banner, lieutenant);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnotherOrcWinningSetsNothingOffAndTheBearersWinAddsOneForEachSiteYouControl() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var soldier2 = scn.GetShadowCard("soldier2");
		var guard = scn.GetFreepsCard("guard");
		var guard2 = scn.GetFreepsCard("guard2");
		scn.MoveCompanionsToTable(guard, guard2);

		scn.StartGame();
		scn.SkipToSite(4);
		// The Free Peoples player took site 1 on a turn of his opponent's; the Shadow player controls 2 and 3.
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(soldier, soldier2);
		scn.AttachCardsTo(soldier, banner);
		assertAttachedTo(banner, soldier);
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{guard, soldier2}, new PhysicalCardImpl[]{guard2, soldier});

		// The other Orc Soldier, bearing nothing, beats the first Guard 7 to 6: nothing is offered.
		scn.FreepsResolveSkirmish(guard);
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(banner));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The bearer beats the second Guard 8 to 6.
		scn.FreepsResolveSkirmish(guard2);
		int twilight = scn.GetTwilight();
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard2));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner));
		scn.ShadowAcceptOptionalTrigger();

		// Two sites of the Shadow player's: (2). The site the other player controls is not counted.
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneSiteControlledTheBearersWinAddsOne() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, banner);
		assertAttachedTo(banner, soldier);
		assertEquals(1, scn.GetShadowControlledSiteCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, soldier);
		scn.FreepsResolveSkirmish(guard);
		int twilight = scn.GetTwilight();
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DecliningTheOfferAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, banner);
		assertAttachedTo(banner, soldier);
		assertEquals(1, scn.GetShadowControlledSiteCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, soldier);
		scn.FreepsResolveSkirmish(guard);
		int twilight = scn.GetTwilight();
		scn.PassSkirmishActions();

		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner));
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void TheBearerLosingASkirmishSetsNothingOff() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);
		// Strength 9 by hand, so that the Guard beats the bearer's 8 and the Soldier (vitality 2) lives through it.
		scn.ApplyAdHocModifier(new StrengthModifier(null, guard, null, 3));

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, banner);
		assertAttachedTo(banner, soldier);
		assertEquals(1, scn.GetShadowControlledSiteCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, soldier);
		scn.FreepsResolveSkirmish(guard);
		int twilight = scn.GetTwilight();
		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(soldier));
		assertAttachedTo(banner, soldier);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(banner));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteYouControlTheBearersWinIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var soldier = scn.GetShadowCard("soldier");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(3);
		// Only the other player controls a site.
		scn.FreepsTakeControlOfSite();
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, banner);
		assertAttachedTo(banner, soldier);
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(1, scn.GetFreepsControlledSiteCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, soldier);
		scn.FreepsResolveSkirmish(guard);
		int twilight = scn.GetTwilight();
		scn.PassSkirmishActions();

		// (1) for each of no sites adds nothing: no offer.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(banner));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceBearerWinningTwiceIsOfferedTheTwilightEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var pillager = scn.GetShadowCard("pillager");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(guard);

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.MoveMinionsToTable(pillager);
		scn.AttachCardsTo(pillager, banner);
		assertAttachedTo(banner, pillager);
		assertEquals(1, scn.GetShadowControlledSiteCount());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(guard, pillager);
		scn.FreepsResolveSkirmish(guard);
		int twilight = scn.GetTwilight();
		scn.PassSkirmishActions();

		// The Pillager, 9 + 1, beats the Guard's 6.
		assertEquals(1, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 1, scn.GetTwilight());

		// The fierce skirmish: he wins again, and is offered again.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(guard, pillager);
		scn.FreepsResolveSkirmish(guard);
		scn.PassFierceSkirmishActions();

		assertEquals(2, scn.GetWoundsOn(guard));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(banner));
		scn.ShadowAcceptOptionalTrigger();
		assertEquals(twilight + 2, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
