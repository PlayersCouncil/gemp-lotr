package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_069_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dart1", "8_69");
					put("dart2", "8_69");

					put("attea", "8_79");       // Úlairë Attëa, Thrall of The One: an enduring Nazgûl; a regroup ability never used here
					put("lemenya", "1_232");    // Úlairë Lemenya, Lieutenant of Morgul: a Nazgûl neither enduring nor mounted; her text is about a character who bears a [wraith] condition
					put("undead", "16_6");      // Undead of Angmar: an enduring [wraith] Wraith, no Nazgûl; its text needs another [wraith] Wraith, and its ability is never used
					put("wargmaster", "5_66");  // Warg-master: an [isengard] Orc warg-rider, no Nazgûl; fierce while not exhausted; his play trigger is not met (cheated in)
					put("warg", "5_65");        // Warg: [isengard] mount for a warg-rider; a skirmish response never met here

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound [gondor] Man companion; a maneuver ability never used here
					put("guard1", "5_83");      // Household Guard: unbound [rohan] Man companion; valiant and nothing else
					put("guard2", "5_83");
					put("rider", "4_265");      // Elite Rider: unbound [rohan] Man companion; +2 only while skirmishing a wounded minion
					put("sam", "1_311");        // Sam, Son of Hamfast: a Ring-bound companion who is not the Ring-bearer; his abilities are never used
					put("albert", "1_69");      // Albert Dreary, Entertainer from Bree: an ally, no companion; a maneuver ability never used here
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: ally, so that Master of Healing can be played; his start-of-turn heal reaches allies only
					put("healing", "3_22");     // Master of Healing: Fellowship event, "Spot Elrond to heal a companion or ally"
					put("mount1", "4_287");     // Rider's Mount: [rohan] mount for a [rohan] Man; a skirmish trigger never met here
					put("spear", "4_288");      // Rider's Spear: [rohan] hand weapon for a [rohan] Man, a possession that is no mount
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	protected VirtualTableScenario GetMountScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dart", "8_69");
					// Úlairë Lemenya: Nazgûl, strength 9, vitality 2; her text is about a character who bears
					// a [wraith] condition.  Black Steed: a mount for a Nazgûl, strength +2 (and +2 more at a
					// plains site); it is discarded only at an underground site.
					put("lemenya", "1_232");
					put("steed", "1_208");
					put("guard", "5_83");       // Household Guard: [rohan] Man, valiant and nothing else
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a maneuver ability used only on purpose
					// Horse of Rohan: mount, cost 2, not unique.  "To play, spot a [rohan] Man.  Bearer must be
					// a Man, Elf, or Wizard."  Its trigger answers the start of a skirmish its bearer is in.
					put("horse1", "4_283");
					put("horse2", "4_283");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BlackDartStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Black Dart
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Archery:</b> Spot your enduring or mounted Nazgûl to transfer this condition from your support area to an unbound companion. Discard a mount borne by that companion.<br>Bearer cannot heal or bear mounts.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dart1");

		assertEquals("Black Dart", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	// "Discard a mount borne by that companion" is a standing discard, apart from "Bearer cannot ... bear mounts"
	// (Ketura on Q-0223: Black Dart is read as Anduril is, with "an ongoing discard effect"; Q-0229: the discard
	// is continuous).  The mount is on the companion as Black Dart is transferred to him, and is discarded.
	@Test
	public void TransferredToAMountedCompanionItDiscardsHisMountAndNoOtherCompanions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountScenario();

		var guard = scn.GetFreepsCard("guard");
		var aragorn = scn.GetFreepsCard("aragorn");
		var horse1 = scn.GetFreepsCard("horse1");
		var horse2 = scn.GetFreepsCard("horse2");
		var frodo = scn.GetRingBearer();
		var dart = scn.GetShadowCard("dart");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		scn.MoveCompanionsToTable(guard, aragorn);
		scn.AttachCardsTo(guard, horse1);
		scn.AttachCardsTo(aragorn, horse2);
		scn.MoveCardsToSupportArea(dart);
		assertAttachedTo(horse1, guard);
		assertAttachedTo(horse2, aragorn);
		assertInZone(Zone.SUPPORT, dart);

		scn.StartGame();
		scn.MoveMinionsToTable(lemenya);
		// A mounted Nazgûl
		scn.AttachCardsTo(lemenya, steed);
		assertAttachedTo(steed, lemenya);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(dart));
		scn.ShadowUseCardAction(dart);
		// An unbound companion: not the Ring-bearer
		assertTrue(scn.ShadowHasCardChoiceAvailable(guard, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo));
		scn.ShadowChooseCard(guard);

		assertAttachedTo(dart, guard);
		assertInZone(Zone.DISCARD, horse1);
		// Another companion's mount, and the Nazgûl's own, are not borne by the Dart's bearer
		assertAttachedTo(horse2, aragorn);
		assertAttachedTo(steed, lemenya);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
	}

	// Comprehensive Rules 5.0, "hinder": while Black Dart is hindered its text does not apply, so its bearer may
	// take up a mount.  When it is restored the standing discard is in force again and the mount is discarded
	// (Ketura on Q-0223).  The restore here is the Shadow player's reconcile, which comes in the regroup phase
	// before the Free Peoples player chooses to move or stay.
	@Test
	public void AMountItsBearerTookUpWhileItWasHinderedIsDiscardedWhenItIsRestored() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetMountScenario();

		var guard = scn.GetFreepsCard("guard");
		var horse = scn.GetFreepsCard("horse1");
		var dart = scn.GetShadowCard("dart");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, dart);
		scn.MoveCardsToHand(horse);
		assertAttachedTo(dart, guard);

		scn.StartGame();

		// "Bearer cannot ... bear mounts": the Guard is the only Man, Elf or Wizard on the table
		assertFalse(scn.FreepsPlayAvailable(horse));

		scn.HinderCard(dart);
		scn.FreepsRefreshDecision();
		assertTrue(scn.FreepsPlayAvailable(horse));
		scn.FreepsPlayCard(horse);
		assertAttachedTo(horse, guard);
		assertTrue(scn.IsHindered(dart));

		// No minion: the Shadow phase is followed by the regroup phase
		scn.SkipToPhase(Phase.REGROUP);
		assertTrue(scn.IsHindered(dart));
		assertAttachedTo(horse, guard);

		scn.PassRegroupActions();
		assertTrue(scn.FreepsDecisionAvailable("another move"));
		assertFalse(scn.IsHindered(dart));
		assertAttachedTo(dart, guard);
		assertInZone(Zone.DISCARD, horse);
	}

	@Test
	public void WithAnEnduringNazgulTheCopyUsedGoesToTheUnboundCompanionChosenAndDiscardsHisMountOnly() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dart1 = scn.GetShadowCard("dart1");
		var dart2 = scn.GetShadowCard("dart2");
		var attea = scn.GetShadowCard("attea");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		var sam = scn.GetFreepsCard("sam");
		var albert = scn.GetFreepsCard("albert");
		var mount = scn.GetFreepsCard("mount1");
		var spear = scn.GetFreepsCard("spear");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(albert);
		scn.AttachCardsTo(guard, mount, spear);
		scn.MoveCardsToSupportArea(dart2);
		scn.MoveCardsToHand(dart1);
		assertAttachedTo(mount, guard);
		assertAttachedTo(spear, guard);
		assertInZone(Zone.SUPPORT, dart2);
		assertInHand(dart1);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// Played to the support area, it is borne by nobody, and the Guard keeps his mount.
		assertTrue(scn.ShadowPlayAvailable(dart1));
		scn.ShadowPlayCard(dart1);
		assertInZone(Zone.SUPPORT, dart1);
		assertAttachedTo(mount, guard);
		assertTrue(scn.AwaitingShadowPhaseActions());

		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.ShadowActionAvailable(dart1));
		assertTrue(scn.ShadowActionAvailable(dart2));
		scn.ShadowUseCardAction(dart1);
		// An unbound companion: not the Ring-bearer, the Ring-bound Sam or the ally.
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(frodo, sam, albert));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertAttachedTo(dart1, guard);
		assertInZone(Zone.SUPPORT, dart2);
		// His mount is discarded; his possession that is no mount stays.
		assertInDiscard(mount);
		assertAttachedTo(spear, guard);
		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());

		// The copy on the Guard is no longer in the support area to be transferred; the other still is.
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dart1));
		assertTrue(scn.ShadowActionAvailable(dart2));
	}

	@Test
	public void NotOfferedWithoutAnEnduringOrMountedNazgul() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dart = scn.GetShadowCard("dart1");
		var lemenya = scn.GetShadowCard("lemenya");
		var undead = scn.GetShadowCard("undead");
		var wargmaster = scn.GetShadowCard("wargmaster");
		var warg = scn.GetShadowCard("warg");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(dart);
		assertInZone(Zone.SUPPORT, dart);

		scn.StartGame();
		// A Nazgûl who is neither; an enduring minion who is no Nazgûl; a mounted minion who is no Nazgûl.
		scn.MoveMinionsToTable(lemenya, undead, wargmaster);
		scn.AttachCardsTo(wargmaster, warg);
		assertAttachedTo(warg, wargmaster);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dart));
		assertInZone(Zone.SUPPORT, dart);
	}

	@Test
	public void NotOfferedWithNoUnboundCompanionToTakeIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dart = scn.GetShadowCard("dart1");
		var attea = scn.GetShadowCard("attea");
		var sam = scn.GetFreepsCard("sam");
		var albert = scn.GetFreepsCard("albert");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(albert);
		scn.MoveCardsToSupportArea(dart);
		assertInZone(Zone.SUPPORT, dart);

		scn.StartGame();
		scn.MoveMinionsToTable(attea);
		scn.SkipToPhase(Phase.ARCHERY);
		scn.FreepsPass();

		// Frodo, the Ring-bearer, and Sam, Ring-bound, are the companions; Albert is an ally.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertFalse(scn.ShadowActionAvailable(dart));
		assertInZone(Zone.SUPPORT, dart);
	}

	@Test
	public void ItsBearerIsNotOfferedToAHealThatOtherWoundedCompanionsAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dart = scn.GetShadowCard("dart1");
		var aragorn = scn.GetFreepsCard("aragorn");
		var guard = scn.GetFreepsCard("guard1");
		var sam = scn.GetFreepsCard("sam");
		var elrond = scn.GetFreepsCard("elrond");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(aragorn, guard, sam);
		scn.MoveAlliesToTable(elrond);
		scn.AttachCardsTo(aragorn, dart);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(guard, 1);
		scn.AddWoundsToChar(sam, 1);
		scn.MoveCardsToHand(healing);

		scn.StartGame();
		assertAttachedTo(dart, aragorn);
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(sam));

		assertTrue(scn.FreepsPlayAvailable(healing));
		scn.FreepsPlayCard(healing);
		// The other wounded companions are offered; the bearer is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard, sam));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(guard);

		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ItsBearerIsNotOfferedAsTheBearerOfAMountThatOtherRohanMenAre() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dart = scn.GetShadowCard("dart1");
		var guard1 = scn.GetFreepsCard("guard1");
		var guard2 = scn.GetFreepsCard("guard2");
		var rider = scn.GetFreepsCard("rider");
		var mount = scn.GetFreepsCard("mount1");
		scn.MoveCompanionsToTable(guard1, guard2, rider);
		scn.AttachCardsTo(guard1, dart);
		scn.MoveCardsToHand(mount);

		scn.StartGame();
		assertAttachedTo(dart, guard1);
		assertInHand(mount);

		assertTrue(scn.FreepsPlayAvailable(mount));
		scn.FreepsPlayCard(mount);
		// The other [rohan] Men are offered as bearers; the Dart's bearer is not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(guard2, rider));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard1));
		scn.FreepsChooseCard(guard2);

		assertAttachedTo(mount, guard2);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
