package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_141_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("alliance", "12_141");
					// A second copy: "discard this condition".
					put("alliance2", "12_141");

					// Vigilant Uruk: an [uruk-hai] Uruk-hai lurker, vitality 2; strength +2 for each [uruk-hai] minion
					// not assigned, which no test reads.
					put("vigilant", "11_205");
					// Saruman, Agent of the Dark Lord: an [uruk-hai] lurker who is no Uruk-hai (a Wizard), vitality 4.
					// "Skirmish: Exert Saruman to make an [uruk-hai] minion strength +2" is used only on purpose; no
					// companion is killed for his heal.
					put("saruman", "12_144");
					// Murderous Uruk: an [uruk-hai] minion who is no lurker; strength 8, damage +1, muster (a regroup
					// matter) only.
					put("uruk", "11_195");
					// Prowling Orc: an [orc] lurker, strength 6; its skirmish ability is never used.
					put("prowling", "11_136");

					// Gimli, Son of Gloin: "Skirmish: Exert Gimli to make him strength +2."
					put("gimli", "1_13");
					// Arwen, Daughter of Elrond: an Elf companion, strength 6; +3 only against a Nazgul.
					put("arwen", "1_30");
					// Elven Sword: strength +1 on an Elf; "Skirmish: Exert bearer (or discard 2 cards from hand) to make a
					// minion skirmishing bearer strength -1."
					put("elvensword", "4_64");
					// Calaglin, Elf of Lorien: an ally; "Regroup: Exert Calaglin to wound a minion."
					put("calaglin", "3_10");
				}},
				// Site 2 is Ettenmoors 1_331: "Skirmish: Exert your companion or minion to make that character strength +2."
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DarkAllianceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Dark Alliance
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: <b>Response:</b> If an ally or companion uses a special ability, discard this condition and exert an [uruk-hai] lurker to prevent that.
		*/

		var scn = GetScenario();

		var alliance = scn.GetFreepsCard("alliance");

		assertEquals("Dark Alliance", alliance.getBlueprint().getTitle());
		assertNull(alliance.getBlueprint().getSubtitle());
		assertFalse(alliance.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, alliance.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, alliance.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, alliance.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(alliance, Keyword.SUPPORT_AREA));
		assertEquals(0, alliance.getBlueprint().getTwilightCost());
	}

	@Test
	public void DiscardsItselfAndExertsAnUrukHaiLurkerToPreventACompanionsSkirmishAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var alliance = scn.GetShadowCard("alliance");
		var alliance2 = scn.GetShadowCard("alliance2");
		var vigilant = scn.GetShadowCard("vigilant");
		var saruman = scn.GetShadowCard("saruman");
		var uruk = scn.GetShadowCard("uruk");
		var prowling = scn.GetShadowCard("prowling");
		scn.MoveCardsToSupportArea(alliance, alliance2);

		scn.StartGame();
		scn.MoveMinionsToTable(vigilant, saruman, uruk, prowling);
		assertInZone(Zone.SUPPORT, alliance, alliance2);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Each copy makes its own offer.
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(alliance));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(alliance2));
		scn.ShadowChooseOptionalTrigger(alliance);

		// The copy taken is discarded, and not the other; then an [uruk-hai] lurker of either race exerts.
		assertInDiscard(alliance);
		assertInZone(Zone.SUPPORT, alliance2);
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(vigilant, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(uruk, prowling));
		scn.ShadowChooseCard(vigilant);
		assertEquals(1, scn.GetWoundsOn(vigilant));
		assertEquals(0, scn.GetWoundsOn(saruman));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(prowling));

		// Prevented: Gimli's exertion stays paid and he is not strength +2.  The other copy is not offered again.
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertEquals(6, scn.GetStrength(gimli));
		assertInZone(Zone.SUPPORT, alliance2);
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void PreventsAnAllysRegroupAbility() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var calaglin = scn.GetFreepsCard("calaglin");
		scn.MoveAlliesToTable(calaglin);

		var alliance = scn.GetShadowCard("alliance");
		var vigilant = scn.GetShadowCard("vigilant");
		var prowling = scn.GetShadowCard("prowling");
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(vigilant, prowling);
		assertInZone(Zone.SUPPORT, alliance);

		scn.SkipToPhase(Phase.REGROUP);
		assertEquals(0, scn.GetWoundsOn(calaglin));
		assertEquals(0, scn.GetWoundsOn(vigilant));
		assertEquals(0, scn.GetWoundsOn(prowling));

		scn.FreepsUseCardAction(calaglin);
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(alliance));
		scn.ShadowAcceptOptionalTrigger();

		// The one [uruk-hai] lurker exerts with nothing asked; no minion is wounded.
		assertInDiscard(alliance);
		assertEquals(1, scn.GetWoundsOn(vigilant));
		assertEquals(0, scn.GetWoundsOn(prowling));
		assertEquals(1, scn.GetWoundsOn(calaglin));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void APossessionsASitesAndAMinionsAbilitiesAreNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var arwen = scn.GetFreepsCard("arwen");
		var elvensword = scn.GetFreepsCard("elvensword");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(arwen, gimli);
		scn.AttachCardsTo(arwen, elvensword);

		var alliance = scn.GetShadowCard("alliance");
		var vigilant = scn.GetShadowCard("vigilant");
		var saruman = scn.GetShadowCard("saruman");
		var uruk = scn.GetShadowCard("uruk");
		var prowling = scn.GetShadowCard("prowling");
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(vigilant, saruman, uruk, prowling);
		assertAttachedTo(elvensword, arwen);
		assertInZone(Zone.SUPPORT, alliance);

		scn.SkipToAssignments();
		assertEquals(2, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{arwen, uruk},
				new PhysicalCardImpl[]{gimli, prowling});
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(arwen);

		assertEquals(7, scn.GetStrength(arwen));
		assertEquals(8, scn.GetStrength(uruk));

		// The Elven Sword's ability, paid for by Arwen's exertion: a possession's ability.
		scn.FreepsUseCardAction(elvensword);
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(7, scn.GetStrength(uruk));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		// Saruman's own ability: a minion's.
		scn.ShadowUseCardAction(saruman);
		assertEquals(1, scn.GetWoundsOn(saruman));
		scn.ShadowChooseCard(vigilant);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Ettenmoors' ability, paid for by Arwen's exertion: a site's.
		scn.FreepsUseCardAction(scn.GetCurrentSite());
		scn.FreepsChooseCard(arwen);
		assertEquals(2, scn.GetWoundsOn(arwen));
		assertEquals(9, scn.GetStrength(arwen));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertInZone(Zone.SUPPORT, alliance);

		// 9 against 7: Arwen wins.  The control: Gimli's own ability, on the same board, is answered.
		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsUseCardAction(gimli);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(alliance));
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(vigilant);
		assertInDiscard(alliance);
		assertEquals(1, scn.GetWoundsOn(vigilant));
		assertEquals(6, scn.GetStrength(gimli));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}

	@Test
	public void IsNotOfferedWhileTheOnlyUrukHaiLurkerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCompanionsToTable(gimli);

		var alliance = scn.GetShadowCard("alliance");
		var vigilant = scn.GetShadowCard("vigilant");
		var uruk = scn.GetShadowCard("uruk");
		var prowling = scn.GetShadowCard("prowling");
		scn.MoveCardsToSupportArea(alliance);

		scn.StartGame();
		scn.MoveMinionsToTable(vigilant, uruk, prowling);
		// Vitality 2 with 1 wound: the Vigilant Uruk cannot exert.  The Murderous Uruk (no lurker) and the Prowling Orc
		// (an [orc] lurker) could.
		scn.AddWoundsToChar(vigilant, 1);
		assertEquals(1, scn.GetWoundsOn(vigilant));
		assertInZone(Zone.SUPPORT, alliance);

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(gimli, uruk);
		scn.ShadowDeclineAssignments();
		scn.FreepsResolveSkirmish(gimli);

		assertEquals(6, scn.GetStrength(gimli));
		scn.FreepsUseCardAction(gimli);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable());
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertEquals(8, scn.GetStrength(gimli));
		assertInZone(Zone.SUPPORT, alliance);
		assertEquals(1, scn.GetWoundsOn(vigilant));
		assertEquals(0, scn.GetWoundsOn(uruk));
		assertEquals(0, scn.GetWoundsOn(prowling));
	}
}
