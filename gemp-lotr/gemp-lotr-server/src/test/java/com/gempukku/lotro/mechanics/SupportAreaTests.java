package com.gempukku.lotro.mechanics;

import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

/**
 * What a "support card" is.
 *
 * Neither rules document defines the term.  Comprehensive Rules 4.2 has "support area" ("a row of cards behind
 * your fellowship"), "ally" ("Play them to your support area") and "control" ("When you take control of a site,
 * place that site in your support area").
 *
 * Ketura, 2026-10-07 (Q-0099), asked whether a card borne by a card in the support area is a support card:
 * "'Support' is meant to be shorthand for 'in the support area'.  A card attached to another is on Gemp in the
 * 'attached zone', but ultimately that's an implementation detail; it is still 'in the support area'."
 *
 * So a support card is a card lying in a support area or borne, at any depth, by a card that is: what an ally
 * bears, what a site a player controls bears.  A card a companion or a minion bears is not one.  The definitions
 * say it with the filter `support`.
 *
 * A text that moves its own card "from your support area" (Black Breath, the [gondor] fortifications) means the
 * card lying there, and is not widened: `zone(support)` in the definitions.
 */
public class SupportAreaTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					// Sauron's Reach: Exert 3 [sauron] minions (or spot Sauron) to hinder all Free Peoples support
					// cards. For each card hindered, hinder a Shadow support card. A maneuver event
					put("reach", "103_101");
					put("sauron", "103_100");   // Sauron, Lord of All Middle-earth: fierce only beside 6 companions

					put("arwen", "1_30");       // Arwen, Daughter of Elrond: Elf companion; stronger against a Nazgul
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: Elf ally; heals allies at the start of the turn
					put("orophin", "1_56");     // Orophin, Lorien Bowman: Elf ally; an archery ability
					put("bow1", "1_41");        // Elven Bow: possession, bearer must be an Elf
					put("bow2", "1_41");
					// The Tale of Gil-galad: Free Peoples condition, unique, bearer must be an Elf; a skirmish ability
					put("tale", "1_66");
					// The White Arrows of Lorien: Free Peoples condition, not unique, bearer must be an Elf
					// companion; an archery ability for a bearer who is an archer, which nobody bearing it here is
					put("arrows1", "1_68");
					put("arrows2", "1_68");

					put("armory1", "1_157");    // Uruk-hai Armory: Shadow condition, support area; idle without an Uruk-hai
					put("armory2", "1_157");
					put("pinned", "1_192");     // Pinned Down: Shadow condition, support area; idle without a [moria] archer
					put("palantir", "3_67");    // The Palantir of Orthanc: Shadow artifact, support area; a Shadow-phase ability
					// Weary: Shadow condition. Plays on a companion or ally; answers its bearer being assigned to
					// an [isengard] tracker and nothing else
					put("weary1", "4_212");
					put("weary2", "4_212");
					// Pillage of Rohan: Shadow condition. Plays on a site you control; idle with no Uruk-hai
					// stacked on that site
					put("pillage", "4_167");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North
					// Black Breath: Plays to your support area. Skirmish: Transfer this condition from your
					// support area to a character skirmishing a Nazgul
					put("breath1", "1_207");
					put("breath2", "1_207");
					// Ulaire Lemenya, Lieutenant of Morgul: Nazgul; bearers of [wraith] conditions are strength -2
					put("lemenya", "1_232");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void ACardBorneByACardInASupportAreaIsASupportCardAndOneACompanionBearsIsNot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var reach = scn.GetShadowCard("reach");
		var sauron = scn.GetShadowCard("sauron");
		var frodo = scn.GetRingBearer();
		var arwen = scn.GetFreepsCard("arwen");
		var elrond = scn.GetFreepsCard("elrond");
		var orophin = scn.GetFreepsCard("orophin");
		var bow1 = scn.GetFreepsCard("bow1");
		var bow2 = scn.GetFreepsCard("bow2");
		var tale = scn.GetFreepsCard("tale");
		var arrows1 = scn.GetFreepsCard("arrows1");
		var arrows2 = scn.GetFreepsCard("arrows2");
		var armory1 = scn.GetShadowCard("armory1");
		var armory2 = scn.GetShadowCard("armory2");
		var pinned = scn.GetShadowCard("pinned");
		var palantir = scn.GetShadowCard("palantir");
		var weary1 = scn.GetShadowCard("weary1");
		var weary2 = scn.GetShadowCard("weary2");
		var pillage = scn.GetShadowCard("pillage");

		scn.StartGame();
		// Both players have left site 1 behind, so it can be controlled.
		scn.SkipToSite(2);
		var bagEnd = scn.GetSite(1);
		scn.ShadowTakeControlOfSite(bagEnd);
		assertInZone(Zone.SUPPORT, bagEnd);

		scn.MoveCompanionsToTable(arwen);
		scn.MoveAlliesToTable(elrond, orophin);
		// Borne by a card in the support area: a possession on one ally, a condition on the other.
		scn.AttachCardsTo(orophin, bow1);
		scn.AttachCardsTo(elrond, tale);
		// Borne by a card that a card in the support area bears. This board is a cheat that no game reaches: no
		// card there is can be played on a possession (no definition's target names one). It is kept to show the
		// engine's own reading, that a support card is borne at any depth (Filters.isInSupportArea walks every
		// bearer up to the bottom of the chain), and not as a thing any card does.
		scn.AttachCardsTo(bow1, arrows1);
		// Borne by a companion: the same possession, and the condition the bow bears. (The Tale of Gil-galad is
		// unique: one to a table.)
		scn.AttachCardsTo(arwen, bow2, arrows2);

		// Lying in the Shadow player's support area: three conditions and an artifact.
		scn.MoveCardsToSupportArea(armory1, armory2, pinned, palantir);
		// Borne by a card in a support area: a Shadow condition on the ally, and one on the site the Shadow
		// player controls. Borne by a companion: the same condition as the ally's.
		scn.AttachCardsTo(elrond, weary1);
		scn.AttachCardsTo(bagEnd, pillage);
		scn.AttachCardsTo(arwen, weary2);
		scn.MoveCardsToHand(reach);
		scn.MoveMinionsToTable(sauron);
		scn.SetTwilight(10);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(reach));
		assertFalse(scn.IsHindered(bow1));
		assertFalse(scn.IsHindered(tale));
		assertFalse(scn.IsHindered(arrows1));

		scn.ShadowPlayCard(reach);

		// "All Free Peoples support cards": the two allies, what each bears, and what the bow bears.
		assertTrue(scn.IsHindered(elrond));
		assertTrue(scn.IsHindered(orophin));
		assertTrue(scn.IsHindered(bow1));
		assertTrue(scn.IsHindered(tale));
		assertTrue(scn.IsHindered(arrows1));
		// Not what a companion bears, nor the companions.
		assertFalse(scn.IsHindered(bow2));
		assertFalse(scn.IsHindered(arrows2));
		assertFalse(scn.IsHindered(arwen));
		assertFalse(scn.IsHindered(frodo));

		// 5 cards were hindered, so 5 Shadow support cards are owed, of 6 there are: the four lying in the
		// support area, the one the ally bears and the one the controlled site bears. The one a companion bears
		// is none, and neither is the site, which is a card of no side.
		assertTrue(scn.ShadowHasCardChoiceAvailable(armory1, armory2, pinned, palantir, weary1, pillage));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(weary2, bagEnd, sauron));
		assertEquals(5, scn.ShadowGetChoiceMin());
		assertEquals(5, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(armory1, armory2, pinned, weary1, pillage);

		assertTrue(scn.IsHindered(weary1));
		assertTrue(scn.IsHindered(pillage));
		assertTrue(scn.IsHindered(armory1));
		assertFalse(scn.IsHindered(palantir));
		assertFalse(scn.IsHindered(weary2));
		assertFalse(scn.IsHindered(bagEnd));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ACardAnAllyBearsIsNotLyingInItsOwnersSupportAreaToBeTransferredFromIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var elrond = scn.GetFreepsCard("elrond");
		var breath1 = scn.GetShadowCard("breath1");
		var breath2 = scn.GetShadowCard("breath2");
		var lemenya = scn.GetShadowCard("lemenya");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(breath1);
		// A Black Breath that an earlier skirmish left on an ally: in a support area, the Free Peoples player's,
		// and not lying in the Shadow player's own.
		scn.AttachCardsTo(elrond, breath2);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignAndResolve(aragorn, lemenya);
		scn.FreepsPass();

		// "Transfer this condition from your support area": the one lying there may go, the one on the ally may
		// not.
		assertTrue(scn.ShadowActionAvailable(breath1));
		assertFalse(scn.ShadowActionAvailable(breath2));

		scn.ShadowUseCardAction(breath1);

		assertAttachedTo(breath1, aragorn);
		assertAttachedTo(breath2, elrond);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
