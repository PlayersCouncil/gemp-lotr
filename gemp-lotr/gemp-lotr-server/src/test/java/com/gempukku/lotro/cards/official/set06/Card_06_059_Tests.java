package com.gempukku.lotro.cards.official.set06;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;
import static com.gempukku.lotro.framework.Assertions.*;

public class Card_06_059_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("banner", "6_59");

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5, vitality 3; Damage +1 is its whole text
					put("worker", "3_62");      // Isengard Worker: [isengard] Orc, strength 5, vitality 3; a regroup ability used only on purpose
					put("guard", "10_81");      // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9, vitality 2; Damage +1 is its whole text
					put("legion", "17_129");    // White Hand Legion: [uruk-hai] Uruk-hai, strength 17; damage +1, to play remove 2 threats (cheated in)
					// Uruk Pursuer: [isengard] Uruk-hai printed fierce, strength 5, vitality 3; tracker; his texts are about
					// unbound Hobbits, and none is on the table
					put("pursuer", "4_190");

					put("gimli", "1_13");       // Gimli, Son of Gloin: strength 6, vitality 3; a skirmish ability used only on purpose
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability used only on purpose
					put("grimir", "1_17");      // Grimir, Dwarven Elder: an ally, home site 3; a fellowship ability used only on purpose
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BannerofIsengardStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 6
		 * Name: Banner of Isengard
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 0
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +1
		 * Game Text: Bearer must be an [isengard] minion.
		 * Each time bearer wins a skirmish, the Free Peoples player must exert a companion for each site you control.
		*/

		var scn = GetScenario();

		var card = scn.GetShadowCard("banner");

		assertEquals("Banner of Isengard", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(0, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlayedOnAnIsengardMinionOfAnyRaceAndNotOnAnUrukHaiOfAnotherCulture() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var banner = scn.GetShadowCard("banner");
		var savage = scn.GetShadowCard("savage");
		var worker = scn.GetShadowCard("worker");
		var guard = scn.GetShadowCard("guard");
		var legion = scn.GetShadowCard("legion");
		scn.MoveCardsToHand(banner);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, worker, guard, legion);
		assertInHand(banner);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(banner));
		scn.ShadowPlayCard(banner);
		// An [isengard] Uruk-hai and an [isengard] Orc; neither the [sauron] nor the [uruk-hai] Uruk-hai.
		assertTrue(scn.ShadowHasCardChoiceAvailable(savage, worker));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(guard, legion));
		assertEquals(1, scn.ShadowGetChoiceMin());

		scn.ShadowChooseCard(worker);
		assertInZone(Zone.ATTACHED, banner);
		assertEquals(worker, banner.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenBearerWinsTheFreePeoplesPlayerExertsACompanionForTheOneSiteItsOwnerControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		var grimir = scn.GetFreepsCard("grimir");
		scn.MoveCompanionsToTable(gimli, aragorn);
		scn.MoveAlliesToTable(grimir);

		var banner = scn.GetShadowCard("banner");
		var savage = scn.GetShadowCard("savage");
		var guard = scn.GetShadowCard("guard");

		// Both markers are on site 3 on the first player's third turn: site 1 is the Free Peoples player's, site 2
		// the Shadow player's.
		scn.StartGame();
		scn.SkipToSite(3);
		scn.FreepsTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.FreepsControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		scn.MoveMinionsToTable(guard, savage);
		scn.AttachCardsTo(savage, banner);
		assertEquals(savage, banner.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, guard}, new PhysicalCardImpl[]{gimli, savage});
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(grimir));

		// The Guard, an [sauron] Uruk-hai with no Banner, 9 against 8: it wins, and nobody exerts.
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertTrue(scn.FreepsAwaitingSkirmishChoice());

		// The bearer, 5 + 1 against 6, wins the tie: the skirmish damage first, which exhausts Gimli, then the Banner.
		scn.FreepsResolveSkirmish(gimli);
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(gimli));

		// The Free Peoples player chooses: a companion who can exert; not the exhausted Gimli, the ally or a minion.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(gimli, grimir, savage, guard));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(aragorn);
		assertEquals(3, scn.GetWoundsOn(aragorn));

		// One exertion: the site the Free Peoples player controls is not counted.
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(grimir));
		assertEquals(0, scn.GetWoundsOn(savage));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithTwoSitesControlledTwoExertionsAreOwedAndOneCompanionMayTakeBoth() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var banner = scn.GetShadowCard("banner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertTrue(scn.ShadowControls(scn.GetSite(1)));
		assertTrue(scn.ShadowControls(scn.GetSite(2)));
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, banner);
		assertEquals(savage, banner.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, savage);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(gimli));

		// Two exertions, each chosen in turn; Aragorn may take the second as well (CR 4.2 "for each").
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn));
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void NobodyExertsWhenTheBearerLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var banner = scn.GetShadowCard("banner");
		var savage = scn.GetShadowCard("savage");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		scn.ShadowTakeControlOfSite();
		assertEquals(2, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(savage);
		scn.AttachCardsTo(savage, banner);
		assertEquals(savage, banner.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, savage);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(0, scn.GetWoundsOn(savage));

		// The bearer, 5 + 1 against 8, loses and lives.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void AFierceBearerWinningTwiceInATurnHasACompanionExertEachTime() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(gimli, aragorn);

		var banner = scn.GetShadowCard("banner");
		var pursuer = scn.GetShadowCard("pursuer");

		scn.StartGame();
		scn.SkipToSite(3);
		scn.ShadowTakeControlOfSite();
		assertEquals(1, scn.GetShadowControlledSiteCount());
		scn.MoveMinionsToTable(pursuer);
		scn.AttachCardsTo(pursuer, banner);
		assertEquals(pursuer, banner.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(aragorn));

		// The bearer, 5 + 1 against 6, wins the tie: one exertion.
		scn.PassSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The fierce skirmish: Gimli again, and he loses again: a second exertion.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(gimli, pursuer);
		scn.FreepsResolveSkirmish(gimli);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(gimli));
		scn.FreepsChooseCard(aragorn);
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
