package com.gempukku.lotro.cards.official.set05;

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
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_068_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("wolf", "5_68");
					put("rider", "5_67");      // Warg-rider: [isengard] Orc, strength 5, vitality 2; fierce while not exhausted
					put("warg", "5_65");       // Warg: [isengard] mount, strength +3, vitality +1, borne by a warg-rider
					put("master", "5_66");     // Warg-master: [isengard] Orc warg-rider, strength 6, vitality 2; fierce while not exhausted
					put("warwarg", "5_64");    // War-warg: [isengard] mount, strength +4, vitality +2, borne by a warg-rider
					put("banner", "6_59");     // Banner of Isengard: [isengard] hand weapon, no mount, strength +1; a trigger on bearer's win
					put("retainer", "3_57");   // Isengard Retainer: [isengard] Orc, strength 8, vitality 3; a regroup ability
					put("savage", "1_151");    // Uruk Savage: an [isengard] Uruk-hai, strength 5, damage +1 only
					put("soldier", "1_271");   // Orc Soldier: a [sauron] Orc, strength 7; a skirmish ability used only on purpose

					put("aragorn", "1_89");    // Aragorn: strength 8, vitality 4
					put("gimli", "1_13");      // Gimli: strength 6, vitality 3
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WolfvoicesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Wolf-voices
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: <b>Skirmish:</b> Make an [isengard] Orc strength +2 (and heal it if mounted).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("wolf");

		assertEquals("Wolf-voices", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAMountedIsengardOrcStrengthPlus2AndHealsItUntilTheSkirmishEnds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(aragorn, gimli);

		var wolf = scn.GetShadowCard("wolf");
		var rider = scn.GetShadowCard("rider");
		var warg = scn.GetShadowCard("warg");
		var master = scn.GetShadowCard("master");
		var warwarg = scn.GetShadowCard("warwarg");
		var retainer = scn.GetShadowCard("retainer");
		var savage = scn.GetShadowCard("savage");
		var soldier = scn.GetShadowCard("soldier");
		scn.MoveCardsToHand(wolf);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, master, retainer, savage, soldier);
		scn.AttachCardsTo(rider, warg);
		scn.AttachCardsTo(master, warwarg);
		scn.AddWoundsToChar(rider, 1);
		scn.AddWoundsToChar(master, 1);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(master));
		assertInZone(Zone.ATTACHED, warg, warwarg);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, rider}, new PhysicalCardImpl[]{gimli, master});
		// The Retainer, the Savage and the Soldier stay out of every skirmish.
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		// 5 + 3 for the Warg; 6 + 4 for the War-warg.
		assertEquals(8, scn.GetStrength(rider));
		assertEquals(10, scn.GetStrength(master));
		assertEquals(8, scn.GetStrength(retainer));
		assertTrue(scn.ShadowPlayAvailable(wolf));

		scn.ShadowPlayCard(wolf);
		// An [isengard] Orc in this skirmish, in another or in none; not an [isengard] Uruk-hai, nor a [sauron] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, master, retainer));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(savage, soldier, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(rider);

		assertInDiscard(wolf);
		assertEquals(10, scn.GetStrength(rider));
		assertEquals(0, scn.GetWoundsOn(rider));
		// The Warg-master, mounted and wounded too, is not the Orc chosen: neither stronger nor healed.
		assertEquals(10, scn.GetStrength(master));
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(8, scn.GetStrength(retainer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Still +2 at the next decision of this skirmish; gone once the next skirmish has begun.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(rider));
		scn.ShadowPass();
		scn.FreepsResolveSkirmish(gimli);
		assertEquals(8, scn.GetStrength(rider));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AnIsengardOrcBearingAPossessionThatIsNoMountIsMadeStrongerAndNotHealed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var wolf = scn.GetShadowCard("wolf");
		var rider = scn.GetShadowCard("rider");
		var banner = scn.GetShadowCard("banner");
		var master = scn.GetShadowCard("master");
		var warwarg = scn.GetShadowCard("warwarg");
		var retainer = scn.GetShadowCard("retainer");
		scn.MoveCardsToHand(wolf);

		scn.StartGame();
		scn.MoveMinionsToTable(rider, master, retainer);
		scn.AttachCardsTo(rider, banner);
		// The Warg-master is a mounted, wounded Orc beside the rider, who is not mounted: the heal turns on the Orc chosen.
		scn.AttachCardsTo(master, warwarg);
		scn.AddWoundsToChar(rider, 1);
		scn.AddWoundsToChar(master, 1);
		scn.AddWoundsToChar(retainer, 1);
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(1, scn.GetWoundsOn(retainer));
		assertInZone(Zone.ATTACHED, banner, warwarg);
		assertInHand(wolf);

		// Not in the assignment phase, the Shadow player's last chance to act before the skirmishes.
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(wolf));
		scn.ShadowPass();

		scn.FreepsAssignAndResolve(aragorn, rider);
		scn.FreepsPass();

		// 5 + 1 for the Banner.
		assertEquals(6, scn.GetStrength(rider));
		// 6 + 4 for the War-warg.
		assertEquals(10, scn.GetStrength(master));
		assertEquals(8, scn.GetStrength(retainer));
		assertTrue(scn.ShadowPlayAvailable(wolf));

		scn.ShadowPlayCard(wolf);
		assertTrue(scn.ShadowHasCardChoiceAvailable(rider, master, retainer));
		scn.ShadowChooseCard(rider);

		// The Banner is a hand weapon: the rider is not mounted, and his wound stays.
		assertInDiscard(wolf);
		assertEquals(8, scn.GetStrength(rider));
		assertEquals(1, scn.GetWoundsOn(rider));
		assertEquals(10, scn.GetStrength(master));
		assertEquals(1, scn.GetWoundsOn(master));
		assertEquals(8, scn.GetStrength(retainer));
		assertEquals(1, scn.GetWoundsOn(retainer));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
