package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_228_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("eowyn", "7_228");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6, valiant and nothing else

					// Mounted minions, and a minion bearing a possession that is no mount.
					put("otsea", "1_235");      // Ulaire Otsea, Lieutenant of Morgul: a Nazgul, strength 9, not fierce; a Shadow ability used only on purpose
					put("steed", "1_208");      // Black Steed: [wraith] mount on a Nazgul; strength +2 more at a plains (site 2 is one)
					put("chieftain", "1_266");  // Orc Chieftain: a [sauron] Orc, strength 7, vitality 2; its text answers a weapon played on it, never here
					put("warg", "10_89");       // Gothmog's Warg: [sauron] mount on a [sauron] Orc; strength +1, +1 for each site its owner controls (none)
					put("banner", "6_98");      // Banner of the Eye: [sauron] hand weapon on a [sauron] Orc, strength +1; an optional trigger on a win
					put("underling", "11_125"); // Isengard Underling: an [orc] Orc, strength 7; its text answers its own play, never here
					put("vicious", "17_92");    // Vicious Warg: [orc] mount on an [orc] Orc of strength 9 or less, strength +4; bearer is fierce

					// Fierce minions, printed so.
					put("pursuer", "4_190");    // Uruk Pursuer: an [isengard] Uruk-hai, strength 5, vitality 3, fierce; its texts are about unbound Hobbits
					put("rider", "12_161");     // Black Rider: a [wraith] Nazgul, fierce, strength 8 +1 for each companion

					// Savage Fury: "Maneuver: Discard a minion to make your [isengard] minion fierce or relentless until the
					// regroup phase."  How a minion comes to be relentless and not fierce.
					put("fury", "103_39");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, strength 5, vitality 3; damage +1 is its whole text
					put("runner", "1_178");     // Goblin Runner: discarded to pay for Savage Fury
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void EowynStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Éowyn, Dernhelm
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Rohan
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Signet: Theoden
		 * Game Text: Valiant.
		 * 	While skirmishing a mounted minion, Éowyn is strength +2.
		 * 	While skirmishing a fierce minion, Éowyn is strength +2.
		 */

		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");

		assertEquals("Éowyn", eowyn.getBlueprint().getTitle());
		assertEquals("Dernhelm", eowyn.getBlueprint().getSubtitle());
		assertTrue(eowyn.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, eowyn.getBlueprint().getSide());
		assertEquals(Culture.ROHAN, eowyn.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, eowyn.getBlueprint().getCardType());
		assertEquals(Race.MAN, eowyn.getBlueprint().getRace());
		assertTrue(scn.HasKeyword(eowyn, Keyword.VALIANT));
		assertEquals(2, eowyn.getBlueprint().getTwilightCost());
		assertEquals(6, eowyn.getBlueprint().getStrength());
		assertEquals(3, eowyn.getBlueprint().getVitality());
		assertEquals(6, eowyn.getBlueprint().getResistance());
		assertEquals(Signet.THEODEN, eowyn.getBlueprint().getSignet());
	}

	@Test
	public void SheIsStrengthPlus2OnlyWhileSkirmishingAMountedMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var otsea = scn.GetShadowCard("otsea");
		var steed = scn.GetShadowCard("steed");
		var chieftain = scn.GetShadowCard("chieftain");
		var warg = scn.GetShadowCard("warg");
		scn.MoveCompanionsToTable(eowyn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(otsea, chieftain);
		scn.AttachCardsTo(otsea, steed);
		assertAttachedTo(steed, otsea);
		scn.AttachCardsTo(chieftain, warg);
		assertAttachedTo(warg, chieftain);

		assertEquals(6, scn.GetStrength(eowyn));
		assertFalse(scn.HasKeyword(otsea, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { eowyn, otsea },
				new PhysicalCardImpl[] { guard, chieftain }
		);

		// Assigned to a mounted minion, but not yet skirmishing it.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(6, scn.GetStrength(eowyn));

		scn.FreepsResolveSkirmish(guard);

		// A mounted minion in a skirmish that is not hers; the Guard, skirmishing it, keeps his own strength.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(6, scn.GetStrength(guard));

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(eowyn);

		// Skirmishing the mounted Nazgul, who is not fierce: +2, and no more.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(eowyn));
	}

	@Test
	public void AMinionBearingAPossessionThatIsNoMountGivesHerNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var chieftain = scn.GetShadowCard("chieftain");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(chieftain);
		scn.AttachCardsTo(chieftain, banner);
		assertAttachedTo(banner, chieftain);

		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(8, scn.GetStrength(chieftain));    // 7 + 1 for the Banner
		assertFalse(scn.HasKeyword(chieftain, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, chieftain);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(eowyn));
	}

	@Test
	public void SheIsStrengthPlus2OnlyWhileSkirmishingAFierceMinionInEitherRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var guard = scn.GetFreepsCard("guard");
		var pursuer = scn.GetShadowCard("pursuer");
		var rider = scn.GetShadowCard("rider");
		scn.MoveCompanionsToTable(eowyn, guard);

		scn.StartGame();
		scn.MoveMinionsToTable(pursuer, rider);

		assertEquals(6, scn.GetStrength(eowyn));
		assertTrue(scn.HasKeyword(pursuer, Keyword.FIERCE));
		assertTrue(scn.HasKeyword(rider, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { eowyn, pursuer },
				new PhysicalCardImpl[] { guard, rider }
		);

		// Assigned to a fierce minion, but not yet skirmishing it.
		assertTrue(scn.FreepsAwaitingSkirmishChoice());
		assertEquals(6, scn.GetStrength(eowyn));

		scn.FreepsResolveSkirmish(guard);

		// A fierce minion in a skirmish that is not hers; the Guard, skirmishing it, keeps his own strength.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(eowyn));
		assertEquals(6, scn.GetStrength(guard));

		// The Guard, 6, loses to the Rider's 11 (8 + 3 companions) and takes a wound.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(guard));
		scn.FreepsResolveSkirmish(eowyn);

		// Skirmishing the fierce Uruk-hai, who bears no mount.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(eowyn));

		// She beats the Pursuer's 5, which lives through it.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(pursuer));
		assertEquals(6, scn.GetStrength(eowyn));

		// The fierce round: the other fierce minion, a Nazgul, on her this time.
		assertTrue(scn.InFierceRound());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[] { eowyn, rider },
				new PhysicalCardImpl[] { guard, pursuer }
		);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(eowyn));
	}

	@Test
	public void SkirmishingAMinionThatIsMountedAndFierceSheIsStrengthPlus4() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var underling = scn.GetShadowCard("underling");
		var vicious = scn.GetShadowCard("vicious");
		scn.MoveCompanionsToTable(eowyn);

		scn.StartGame();
		scn.MoveMinionsToTable(underling);
		scn.AttachCardsTo(underling, vicious);
		assertAttachedTo(vicious, underling);

		// The Warg is a mount, and makes its bearer fierce.
		assertTrue(scn.HasKeyword(underling, Keyword.FIERCE));
		assertEquals(6, scn.GetStrength(eowyn));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, underling);
		scn.FreepsResolveSkirmish(eowyn);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(10, scn.GetStrength(eowyn));     // 6 + 2 + 2
	}

	@Test
	public void AgainstARelentlessMinionWhoIsNotFierceSheGainsNothingInTheRelentlessRound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var fury = scn.GetShadowCard("fury");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCompanionsToTable(eowyn);
		scn.MoveCardsToSupportArea(fury);

		scn.StartGame();
		scn.MoveMinionsToTable(savage, runner);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Runner is discarded to pay; the Savage is the only [isengard] minion, so nothing more is asked once the
		// keyword is chosen.
		scn.ShadowUseCardAction(fury);
		scn.ShadowChooseCard(runner);
		scn.ShadowChoose("Relentless");
		assertTrue(scn.HasKeyword(savage, Keyword.RELENTLESS));
		assertFalse(scn.HasKeyword(savage, Keyword.FIERCE));

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eowyn, savage);
		scn.FreepsResolveSkirmish(eowyn);

		assertEquals(6, scn.GetStrength(eowyn));

		// She beats the Savage's 5, which lives through it.
		scn.PassSkirmishActions();
		assertEquals(1, scn.GetWoundsOn(savage));

		// No minion is fierce, so the next round is the relentless one.
		assertFalse(scn.InFierceRound());
		assertTrue(scn.InRelentlessRound());
		scn.PassAssignmentActions();
		scn.FreepsAssignToMinions(eowyn, savage);
		scn.FreepsResolveSkirmish(eowyn);

		// CR 5.0 "relentless": the minion is not fierce, so she does not gain +2.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.InRelentlessRound());
		assertEquals(6, scn.GetStrength(eowyn));
	}
}
